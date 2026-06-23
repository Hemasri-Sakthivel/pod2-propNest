// Dependency-free Markdown -> .docx (Office Open XML) converter.
// Handles: # headings, paragraphs, **bold**, `code`, bullet lists, > quotes,
// --- rules, ``` code blocks, and | pipe | tables. Good enough for this guide.
const fs = require('fs');
const zlib = require('zlib');

const inFile = process.argv[2];
const outFile = process.argv[3];
const md = fs.readFileSync(inFile, 'utf8');
const lines = md.split(/\r?\n/);

// ---------- XML helpers ----------
function xml(s) {
  return String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
// Build runs from inline markdown (**bold**, `code`). Returns <w:r> XML.
function runs(text, baseRpr = '') {
  // tokenise on **...** and `...`
  const parts = [];
  let i = 0;
  const re = /(\*\*([^*]+)\*\*|`([^`]+)`)/g;
  let m, last = 0;
  while ((m = re.exec(text)) !== null) {
    if (m.index > last) parts.push({ t: text.slice(last, m.index), b: false, c: false });
    if (m[2] !== undefined) parts.push({ t: m[2], b: true, c: false });
    else parts.push({ t: m[3], b: false, c: true });
    last = re.lastIndex;
  }
  if (last < text.length) parts.push({ t: text.slice(last), b: false, c: false });
  if (parts.length === 0) parts.push({ t: '', b: false, c: false });

  return parts.map(p => {
    let rpr = baseRpr;
    if (p.b) rpr += '<w:b/>';
    if (p.c) rpr += '<w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:color w:val="B91C1C"/><w:shd w:val="clear" w:fill="F3F4F6"/>';
    return `<w:r>${rpr ? `<w:rPr>${rpr}</w:rPr>` : ''}<w:t xml:space="preserve">${xml(p.t)}</w:t></w:r>`;
  }).join('');
}
function para(inner, pPr = '') {
  return `<w:p>${pPr ? `<w:pPr>${pPr}</w:pPr>` : ''}${inner}</w:p>`;
}
function heading(level, text) {
  return para(runs(text), `<w:pStyle w:val="Heading${level}"/>`);
}

// ---------- table builder ----------
function buildTable(rows) {
  const header = rows[0];
  const body = rows.slice(1);
  const cols = header.length;
  const tblPr = `<w:tblPr>
    <w:tblStyle w:val="TableGrid"/>
    <w:tblW w:w="5000" w:type="pct"/>
    <w:tblBorders>
      <w:top w:val="single" w:sz="4" w:color="D0D7DE"/>
      <w:left w:val="single" w:sz="4" w:color="D0D7DE"/>
      <w:bottom w:val="single" w:sz="4" w:color="D0D7DE"/>
      <w:right w:val="single" w:sz="4" w:color="D0D7DE"/>
      <w:insideH w:val="single" w:sz="4" w:color="D0D7DE"/>
      <w:insideV w:val="single" w:sz="4" w:color="D0D7DE"/>
    </w:tblBorders>
    <w:tblLayout w:type="autofit"/>
  </w:tblPr>`;
  function cell(text, isHeader) {
    const shd = isHeader ? '<w:shd w:val="clear" w:fill="2563EB"/>' : '';
    const tcPr = `<w:tcPr>${shd}</w:tcPr>`;
    const base = isHeader ? '<w:b/><w:color w:val="FFFFFF"/>' : '';
    return `<w:tc>${tcPr}${para(runs(text, base))}</w:tc>`;
  }
  function row(cells, isHeader) {
    // pad/truncate to column count
    const c = cells.slice(0, cols);
    while (c.length < cols) c.push('');
    return `<w:tr>${c.map(x => cell(x, isHeader)).join('')}</w:tr>`;
  }
  return `<w:tbl>${tblPr}${row(header, true)}${body.map(r => row(r, false)).join('')}</w:tbl>` + para('');
}

// ---------- main parse loop ----------
const out = [];
let i = 0;
function splitRow(line) {
  return line.replace(/^\s*\|/, '').replace(/\|\s*$/, '').split('|').map(c => c.trim());
}

while (i < lines.length) {
  const line = lines[i];

  // code fence
  if (/^```/.test(line)) {
    i++;
    const code = [];
    while (i < lines.length && !/^```/.test(lines[i])) { code.push(lines[i]); i++; }
    i++; // closing fence
    const shd = '<w:shd w:val="clear" w:fill="F6F8FA"/>';
    for (const cl of code) {
      const r = `<w:r><w:rPr><w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/><w:sz w:val="18"/></w:rPr><w:t xml:space="preserve">${xml(cl) || ' '}</w:t></w:r>`;
      out.push(para(r, `${shd}<w:spacing w:after="0" w:line="240" w:lineRule="auto"/><w:ind w:left="120"/>`));
    }
    out.push(para(''));
    continue;
  }

  // table
  if (/^\s*\|/.test(line) && i + 1 < lines.length && /^\s*\|?[\s:|-]+\|/.test(lines[i + 1]) && /-/.test(lines[i + 1])) {
    const rows = [splitRow(line)];
    i += 2; // header + separator
    while (i < lines.length && /^\s*\|/.test(lines[i])) { rows.push(splitRow(lines[i])); i++; }
    out.push(buildTable(rows));
    continue;
  }

  // heading
  let h = /^(#{1,6})\s+(.*)$/.exec(line);
  if (h) {
    const lvl = Math.min(h[1].length, 4);
    out.push(heading(lvl, h[2]));
    i++; continue;
  }

  // horizontal rule
  if (/^---+\s*$/.test(line)) {
    out.push(para('', '<w:pBdr><w:bottom w:val="single" w:sz="6" w:space="1" w:color="2563EB"/></w:pBdr>'));
    i++; continue;
  }

  // blockquote
  if (/^>\s?/.test(line)) {
    const buf = [];
    while (i < lines.length && /^>\s?/.test(lines[i])) { buf.push(lines[i].replace(/^>\s?/, '')); i++; }
    const pPr = '<w:pBdr><w:left w:val="single" w:sz="18" w:space="6" w:color="2563EB"/></w:pBdr><w:shd w:val="clear" w:fill="EFF6FF"/><w:ind w:left="200"/>';
    out.push(para(runs(buf.join(' ')), pPr));
    continue;
  }

  // bullet list
  let b = /^\s*[-*]\s+(.*)$/.exec(line);
  if (b) {
    const pPr = '<w:ind w:left="360" w:hanging="200"/><w:spacing w:after="40"/>';
    out.push(para(runs('•  ' + b[1]), pPr));
    i++; continue;
  }

  // blank
  if (/^\s*$/.test(line)) { i++; continue; }

  // normal paragraph
  out.push(para(runs(line)));
  i++;
}

// ---------- docx parts ----------
const documentXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:body>
${out.join('\n')}
<w:sectPr><w:pgSz w:w="12240" w:h="15840"/><w:pgMar w:top="1100" w:right="1100" w:bottom="1100" w:left="1100"/></w:sectPr>
</w:body>
</w:document>`;

const stylesXml = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
<w:docDefaults><w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr></w:rPrDefault>
<w:pPrDefault><w:pPr><w:spacing w:after="120" w:line="276" w:lineRule="auto"/></w:pPr></w:pPrDefault></w:docDefaults>
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="360" w:after="120"/><w:outlineLvl w:val="0"/><w:pBdr><w:bottom w:val="single" w:sz="12" w:space="2" w:color="2563EB"/></w:pBdr></w:pPr><w:rPr><w:b/><w:color w:val="1E3A8A"/><w:sz w:val="36"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="heading 2"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="280" w:after="100"/><w:outlineLvl w:val="1"/><w:pBdr><w:bottom w:val="single" w:sz="4" w:space="2" w:color="93C5FD"/></w:pBdr></w:pPr><w:rPr><w:b/><w:color w:val="1D4ED8"/><w:sz w:val="28"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="heading 3"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="220" w:after="80"/><w:outlineLvl w:val="2"/></w:pPr><w:rPr><w:b/><w:color w:val="1F2937"/><w:sz w:val="24"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading4"><w:name w:val="heading 4"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/><w:pPr><w:keepNext/><w:spacing w:before="160" w:after="60"/><w:outlineLvl w:val="3"/></w:pPr><w:rPr><w:b/><w:i/><w:color w:val="374151"/><w:sz w:val="22"/></w:rPr></w:style>
<w:style w:type="table" w:styleId="TableGrid"><w:name w:val="Table Grid"/><w:tblPr><w:tblBorders><w:top w:val="single" w:sz="4" w:color="D0D7DE"/><w:left w:val="single" w:sz="4" w:color="D0D7DE"/><w:bottom w:val="single" w:sz="4" w:color="D0D7DE"/><w:right w:val="single" w:sz="4" w:color="D0D7DE"/><w:insideH w:val="single" w:sz="4" w:color="D0D7DE"/><w:insideV w:val="single" w:sz="4" w:color="D0D7DE"/></w:tblBorders></w:tblPr></w:style>
</w:styles>`;

const contentTypes = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>`;

const rels = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>`;

const docRels = `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>`;

// ---------- minimal ZIP writer (STORE, no compression) with CRC32 ----------
const crcTable = (() => {
  const t = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) c = c & 1 ? 0xEDB88320 ^ (c >>> 1) : c >>> 1;
    t[n] = c >>> 0;
  }
  return t;
})();
function crc32(buf) {
  let c = 0xFFFFFFFF;
  for (let i = 0; i < buf.length; i++) c = crcTable[(c ^ buf[i]) & 0xFF] ^ (c >>> 8);
  return (c ^ 0xFFFFFFFF) >>> 0;
}

const files = [
  { name: '[Content_Types].xml', data: Buffer.from(contentTypes, 'utf8') },
  { name: '_rels/.rels', data: Buffer.from(rels, 'utf8') },
  { name: 'word/document.xml', data: Buffer.from(documentXml, 'utf8') },
  { name: 'word/styles.xml', data: Buffer.from(stylesXml, 'utf8') },
  { name: 'word/_rels/document.xml.rels', data: Buffer.from(docRels, 'utf8') },
];

const localParts = [];
const central = [];
let offset = 0;
for (const f of files) {
  const nameBuf = Buffer.from(f.name, 'utf8');
  const comp = zlib.deflateRawSync(f.data);
  const crc = crc32(f.data);

  const local = Buffer.alloc(30);
  local.writeUInt32LE(0x04034b50, 0);   // local file header sig
  local.writeUInt16LE(20, 4);            // version needed
  local.writeUInt16LE(0, 6);             // flags
  local.writeUInt16LE(8, 8);             // method 8 = deflate
  local.writeUInt16LE(0, 10);            // mod time
  local.writeUInt16LE(0x21, 12);         // mod date (fixed)
  local.writeUInt32LE(crc, 14);
  local.writeUInt32LE(comp.length, 18);
  local.writeUInt32LE(f.data.length, 22);
  local.writeUInt16LE(nameBuf.length, 26);
  local.writeUInt16LE(0, 28);
  localParts.push(local, nameBuf, comp);

  const cen = Buffer.alloc(46);
  cen.writeUInt32LE(0x02014b50, 0);      // central dir sig
  cen.writeUInt16LE(20, 4);              // version made by
  cen.writeUInt16LE(20, 6);              // version needed
  cen.writeUInt16LE(0, 8);
  cen.writeUInt16LE(8, 10);
  cen.writeUInt16LE(0, 12);
  cen.writeUInt16LE(0x21, 14);
  cen.writeUInt32LE(crc, 16);
  cen.writeUInt32LE(comp.length, 20);
  cen.writeUInt32LE(f.data.length, 24);
  cen.writeUInt16LE(nameBuf.length, 28);
  cen.writeUInt16LE(0, 30);
  cen.writeUInt16LE(0, 32);
  cen.writeUInt16LE(0, 34);
  cen.writeUInt16LE(0, 36);
  cen.writeUInt32LE(0, 38);
  cen.writeUInt32LE(offset, 42);
  central.push(cen, nameBuf);

  offset += local.length + nameBuf.length + comp.length;
}

const centralBuf = Buffer.concat(central);
const localBuf = Buffer.concat(localParts);
const end = Buffer.alloc(22);
end.writeUInt32LE(0x06054b50, 0);
end.writeUInt16LE(0, 4);
end.writeUInt16LE(0, 6);
end.writeUInt16LE(files.length, 8);
end.writeUInt16LE(files.length, 10);
end.writeUInt32LE(centralBuf.length, 12);
end.writeUInt32LE(localBuf.length, 16);
end.writeUInt16LE(0, 20);

fs.writeFileSync(outFile, Buffer.concat([localBuf, centralBuf, end]));
console.log('Wrote ' + outFile + ' (' + out.length + ' blocks)');
