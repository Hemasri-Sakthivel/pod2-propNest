// Minimal, dependency-free Markdown -> standalone HTML converter.
// Handles the subset used in CONCEPTS_USED.md: headings, tables, lists,
// blockquotes, hr, bold, inline code. Good enough for a clean printable doc.
const fs = require('fs');

const inFile = process.argv[2];
const outFile = process.argv[3];
const src = fs.readFileSync(inFile, 'utf8');
const lines = src.split(/\r?\n/);

function esc(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}
// inline: escape first, then apply bold + inline code on the escaped text
function inline(s) {
  let t = esc(s);
  t = t.replace(/`([^`]+)`/g, (_, c) => `<code>${c}</code>`);
  t = t.replace(/\*\*([^*]+)\*\*/g, (_, c) => `<strong>${c}</strong>`);
  return t;
}

const out = [];
let i = 0;
let inList = false;
function closeList() { if (inList) { out.push('</ul>'); inList = false; } }

while (i < lines.length) {
  const line = lines[i];

  // table: a line with | followed by a |---| separator
  if (/^\s*\|/.test(line) && i + 1 < lines.length && /^\s*\|?[\s:|-]+\|/.test(lines[i + 1]) && /-/.test(lines[i + 1])) {
    closeList();
    const header = line.split('|').slice(1, -1).map(c => c.trim());
    i += 2; // skip header + separator
    out.push('<table>');
    out.push('<thead><tr>' + header.map(h => `<th>${inline(h)}</th>`).join('') + '</tr></thead>');
    out.push('<tbody>');
    while (i < lines.length && /^\s*\|/.test(lines[i])) {
      const cells = lines[i].split('|').slice(1, -1).map(c => c.trim());
      out.push('<tr>' + cells.map(c => `<td>${inline(c)}</td>`).join('') + '</tr>');
      i++;
    }
    out.push('</tbody></table>');
    continue;
  }

  // headings
  let m = /^(#{1,6})\s+(.*)$/.exec(line);
  if (m) {
    closeList();
    const lvl = m[1].length;
    out.push(`<h${lvl}>${inline(m[2])}</h${lvl}>`);
    i++;
    continue;
  }

  // hr
  if (/^---+\s*$/.test(line)) { closeList(); out.push('<hr/>'); i++; continue; }

  // blockquote
  if (/^>\s?/.test(line)) {
    closeList();
    const buf = [];
    while (i < lines.length && /^>\s?/.test(lines[i])) { buf.push(lines[i].replace(/^>\s?/, '')); i++; }
    out.push(`<blockquote>${inline(buf.join(' '))}</blockquote>`);
    continue;
  }

  // list item
  m = /^\s*[-*]\s+(.*)$/.exec(line);
  if (m) {
    if (!inList) { out.push('<ul>'); inList = true; }
    out.push(`<li>${inline(m[1])}</li>`);
    i++;
    continue;
  }

  // blank
  if (/^\s*$/.test(line)) { closeList(); i++; continue; }

  // paragraph
  closeList();
  out.push(`<p>${inline(line)}</p>`);
  i++;
}
closeList();

const css = `
:root { color-scheme: light; }
* { box-sizing: border-box; }
body { font-family: "Segoe UI", Roboto, Helvetica, Arial, sans-serif; line-height: 1.55;
  color: #1f2328; max-width: 980px; margin: 0 auto; padding: 40px 32px; background: #fff; }
h1 { font-size: 28px; border-bottom: 3px solid #2563eb; padding-bottom: 8px; margin-top: 8px; }
h2 { font-size: 22px; border-bottom: 1px solid #d0d7de; padding-bottom: 6px; margin-top: 34px; color: #1d4ed8; }
h3 { font-size: 17px; margin-top: 22px; }
p { margin: 10px 0; }
code { background: #f3f4f6; padding: 1px 6px; border-radius: 5px; font-size: 0.86em;
  font-family: "Cascadia Code", Consolas, "Courier New", monospace; color: #b91c1c; }
strong { color: #111827; }
blockquote { border-left: 4px solid #2563eb; background: #eff6ff; margin: 14px 0;
  padding: 8px 16px; border-radius: 0 6px 6px 0; color: #374151; }
ul { margin: 10px 0; padding-left: 24px; }
li { margin: 4px 0; }
hr { border: none; border-top: 1px solid #e5e7eb; margin: 28px 0; }
table { border-collapse: collapse; width: 100%; margin: 14px 0; font-size: 14px; }
th, td { border: 1px solid #d0d7de; padding: 8px 11px; text-align: left; vertical-align: top; }
th { background: #2563eb; color: #fff; font-weight: 600; }
tr:nth-child(even) td { background: #f6f8fa; }
@media print { body { padding: 0; max-width: none; } h2 { page-break-after: avoid; }
  table, blockquote { page-break-inside: avoid; } a { color: inherit; text-decoration: none; } }
`;

const html = `<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1"/>
<title>PropNest — Concepts Used</title>
<style>${css}</style></head>
<body>
${out.join('\n')}
</body></html>`;

fs.writeFileSync(outFile, html, 'utf8');
console.log('Wrote ' + outFile);
