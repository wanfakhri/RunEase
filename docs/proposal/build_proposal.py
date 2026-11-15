"""Build the RunEase project proposal (.docx) with screenshots and diagrams."""

from pathlib import Path

from docx import Document
from docx.enum.section import WD_ORIENT, WD_SECTION
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_BREAK
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Cm, Inches, Pt, RGBColor

BASE = Path(__file__).resolve().parent
SHOTS = BASE / "screenshots"
IMAGES = BASE / "images"
OUT = BASE / "RunEase_Proposal.docx"

NAVY = RGBColor(0x0B, 0x12, 0x20)
INK = RGBColor(0x1C, 0x24, 0x33)
SLATE = RGBColor(0x5C, 0x67, 0x7D)
LIME = RGBColor(0xC9, 0xF0, 0x4C)
GREEN = RGBColor(0x4F, 0x6B, 0x10)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
LINE = "C9D2E0"

MEMBERS = [
    ("2024948805", "WAN MUHAMMAD FAKHRI BIN WAN ZULKIFLI"),
    ("2024142043", "MOHAMAD NAQIUDIN BIN SANI"),
    ("2024768875", "NUR IZZATI NABILAH BINTI ABAS"),
    ("2024587539", "NURFARAHIN BINTI MOHD NAFIS"),
    ("2024979227", "NURUL AINI BINTI ABDULLAH"),
]


# ---------------------------------------------------------------------------
# Low-level helpers
# ---------------------------------------------------------------------------
def set_cell_bg(cell, hex_color):
    tc_pr = cell._tc.get_or_add_tcPr()
    shd = OxmlElement("w:shd")
    shd.set(qn("w:val"), "clear")
    shd.set(qn("w:color"), "auto")
    shd.set(qn("w:fill"), hex_color)
    tc_pr.append(shd)


def set_cell_margins(cell, top=60, bottom=60, left=90, right=90):
    tc = cell._tc
    tc_pr = tc.get_or_add_tcPr()
    tc_mar = tc_pr.first_child_found_in("w:tcMar")
    if tc_mar is None:
        tc_mar = OxmlElement("w:tcMar")
        tc_pr.append(tc_mar)
    for tag, value in (("top", top), ("bottom", bottom), ("left", left), ("right", right)):
        node = tc_mar.find(qn(f"w:{tag}"))
        if node is None:
            node = OxmlElement(f"w:{tag}")
            tc_mar.append(node)
        node.set(qn("w:w"), str(value))
        node.set(qn("w:type"), "dxa")


def set_table_borders(table, color=LINE, size=4):
    tbl = table._tbl
    tbl_pr = tbl.tblPr
    borders = OxmlElement("w:tblBorders")
    for edge in ("top", "left", "bottom", "right", "insideH", "insideV"):
        el = OxmlElement(f"w:{edge}")
        el.set(qn("w:val"), "single")
        el.set(qn("w:sz"), str(size))
        el.set(qn("w:space"), "0")
        el.set(qn("w:color"), color)
        borders.append(el)
    tbl_pr.append(borders)


def keep_row_together(row):
    tr_pr = row._tr.get_or_add_trPr()
    cant_split = OxmlElement("w:cantSplit")
    tr_pr.append(cant_split)


def set_repeat_header(row):
    tr_pr = row._tr.get_or_add_trPr()
    tbl_header = OxmlElement("w:tblHeader")
    tbl_header.set(qn("w:val"), "true")
    tr_pr.append(tbl_header)


def add_field(paragraph, instr):
    run = paragraph.add_run()
    fld_begin = OxmlElement("w:fldChar")
    fld_begin.set(qn("w:fldCharType"), "begin")
    instr_el = OxmlElement("w:instrText")
    instr_el.set(qn("xml:space"), "preserve")
    instr_el.text = instr
    fld_sep = OxmlElement("w:fldChar")
    fld_sep.set(qn("w:fldCharType"), "separate")
    text = OxmlElement("w:t")
    text.text = "1"
    fld_end = OxmlElement("w:fldChar")
    fld_end.set(qn("w:fldCharType"), "end")
    run._r.append(fld_begin)
    run._r.append(instr_el)
    run._r.append(fld_sep)
    run._r.append(text)
    run._r.append(fld_end)


def style_document(doc):
    normal = doc.styles["Normal"]
    normal.font.name = "Calibri"
    normal.font.size = Pt(10.5)
    normal.font.color.rgb = INK
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.line_spacing = 1.13

    for name, size, color, before, after in (
        ("Heading 1", 18, NAVY, 16, 8),
        ("Heading 2", 13.5, NAVY, 12, 5),
        ("Heading 3", 11.5, GREEN, 10, 4),
    ):
        style = doc.styles[name]
        style.font.name = "Calibri"
        style.font.size = Pt(size)
        style.font.bold = True
        style.font.color.rgb = color
        style.paragraph_format.space_before = Pt(before)
        style.paragraph_format.space_after = Pt(after)
        style.paragraph_format.keep_with_next = True


def page_setup(section):
    section.orientation = WD_ORIENT.PORTRAIT
    section.page_width = Cm(21.0)
    section.page_height = Cm(29.7)
    section.top_margin = Cm(2.0)
    section.bottom_margin = Cm(1.9)
    section.left_margin = Cm(2.2)
    section.right_margin = Cm(2.2)


def landscape(section):
    section.orientation = WD_ORIENT.LANDSCAPE
    section.page_width = Cm(29.7)
    section.page_height = Cm(21.0)
    section.top_margin = Cm(1.2)
    section.bottom_margin = Cm(1.2)
    section.left_margin = Cm(1.5)
    section.right_margin = Cm(1.5)


def add_header_footer(section, course="CSC584 Enterprise Programming"):
    header = section.header
    p = header.paragraphs[0]
    p.text = ""
    run = p.add_run("RunEase  |  Running Event Management System")
    run.font.size = Pt(8)
    run.font.color.rgb = SLATE
    run.font.name = "Calibri"
    p.alignment = WD_ALIGN_PARAGRAPH.RIGHT
    pPr = p._p.get_or_add_pPr()
    pBdr = OxmlElement("w:pBdr")
    bottom = OxmlElement("w:bottom")
    bottom.set(qn("w:val"), "single")
    bottom.set(qn("w:sz"), "6")
    bottom.set(qn("w:space"), "4")
    bottom.set(qn("w:color"), "C9F04C")
    pBdr.append(bottom)
    pPr.append(pBdr)

    footer = section.footer
    fp = footer.paragraphs[0]
    fp.text = ""
    left = fp.add_run(course + "  \u00b7  Page ")
    left.font.size = Pt(8)
    left.font.color.rgb = SLATE
    add_field(fp, "PAGE")
    fp.alignment = WD_ALIGN_PARAGRAPH.CENTER


def h1(doc, text):
    p = doc.add_paragraph(style="Heading 1")
    p.add_run(text)
    pPr = p._p.get_or_add_pPr()
    pBdr = OxmlElement("w:pBdr")
    bottom = OxmlElement("w:bottom")
    bottom.set(qn("w:val"), "single")
    bottom.set(qn("w:sz"), "14")
    bottom.set(qn("w:space"), "4")
    bottom.set(qn("w:color"), "C9F04C")
    pBdr.append(bottom)
    pPr.append(pBdr)
    return p


def h2(doc, text):
    p = doc.add_paragraph(style="Heading 2")
    p.add_run(text)
    return p


def h3(doc, text):
    p = doc.add_paragraph(style="Heading 3")
    p.add_run(text)
    return p


def para(doc, text, size=None, italic=False, color=None, align=None, space_after=None):
    p = doc.add_paragraph()
    run = p.add_run(text)
    if size:
        run.font.size = Pt(size)
    run.italic = italic
    if color:
        run.font.color.rgb = color
    if align:
        p.alignment = align
    if space_after is not None:
        p.paragraph_format.space_after = Pt(space_after)
    return p


def bullets(doc, items, style="List Bullet"):
    for item in items:
        p = doc.add_paragraph(style=style)
        p.paragraph_format.space_after = Pt(2)
        p.add_run(item)


def numbered(doc, items):
    bullets(doc, items, "List Number")


def caption(doc, text):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.font.size = Pt(8.5)
    run.italic = True
    run.font.color.rgb = SLATE
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_after = Pt(12)
    p.paragraph_format.keep_together = True
    return p


def figure(doc, path, cap, width=6.0):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(2)
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(str(path), width=Inches(width))
    caption(doc, cap)


def data_table(doc, headers, rows, widths=None, header_bg="0B1220", header_color=WHITE,
               font=9, first_col_bold=False):
    table = doc.add_table(rows=1, cols=len(headers))
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table)
    hdr = table.rows[0]
    set_repeat_header(hdr)
    for i, text in enumerate(headers):
        cell = hdr.cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(font)
        run.font.color.rgb = header_color
        set_cell_bg(cell, header_bg)
        set_cell_margins(cell)
        if widths:
            cell.width = Cm(widths[i])
    for r_index, row in enumerate(rows):
        cells = table.add_row().cells
        keep_row_together(table.rows[-1])
        for i, value in enumerate(row):
            cell = cells[i]
            cell.text = ""
            p = cell.paragraphs[0]
            run = p.add_run(str(value))
            run.font.size = Pt(font)
            if first_col_bold and i == 0:
                run.bold = True
            set_cell_margins(cell)
            if r_index % 2 == 1:
                set_cell_bg(cell, "F4F7FC")
            if widths:
                cell.width = Cm(widths[i])
    doc.add_paragraph().paragraph_format.space_after = Pt(2)
    return table


def page_break(doc):
    doc.add_paragraph().add_run().add_break(WD_BREAK.PAGE)


# ---------------------------------------------------------------------------
# Content
# ---------------------------------------------------------------------------
def cover(doc):
    section = doc.sections[0]
    page_setup(section)
    section.different_first_page_header_footer = True

    for _ in range(2):
        doc.add_paragraph()

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("UNIVERSITI TEKNOLOGI MARA")
    r.font.size = Pt(15)
    r.bold = True
    r.font.color.rgb = NAVY
    p.paragraph_format.space_after = Pt(2)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("FACULTY OF COMPUTER AND MATHEMATICAL SCIENCES")
    r.font.size = Pt(10.5)
    r.bold = True
    r.font.color.rgb = SLATE
    p.paragraph_format.space_after = Pt(26)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("CSC584  ·  ENTERPRISE PROGRAMMING")
    r.font.size = Pt(11)
    r.bold = True
    r.font.color.rgb = GREEN

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("GROUP PROJECT PROPOSAL")
    r.font.size = Pt(12)
    r.font.color.rgb = SLATE
    p.paragraph_format.space_after = Pt(16)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("RunEase")
    r.font.size = Pt(46)
    r.bold = True
    r.font.color.rgb = NAVY
    p.paragraph_format.space_after = Pt(0)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("Running Event Management System")
    r.font.size = Pt(18)
    r.font.color.rgb = GREEN
    p.paragraph_format.space_after = Pt(4)

    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("A web-based platform for race discovery, registration, payment, race-day operations and governance")
    r.font.size = Pt(9.5)
    r.italic = True
    r.font.color.rgb = SLATE
    p.paragraph_format.space_after = Pt(28)

    table = doc.add_table(rows=1, cols=3)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table, color="0B1220", size=6)
    headers = ("No.", "Group member", "Student ID")
    for i, text in enumerate(headers):
        cell = table.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(9.5)
        run.font.color.rgb = WHITE
        set_cell_bg(cell, "0B1220")
        set_cell_margins(cell, 90, 90)
    for i, (sid, name) in enumerate(MEMBERS, start=1):
        cells = table.add_row().cells
        keep_row_together(table.rows[-1])
        for j, value in enumerate((str(i), name, sid)):
            cell = cells[j]
            cell.text = ""
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if j != 1 else WD_ALIGN_PARAGRAPH.LEFT
            run = p.add_run(value)
            run.font.size = Pt(9)
            set_cell_margins(cell, 80, 80)
            if i % 2 == 0:
                set_cell_bg(cell, "F4F7FC")
    widths = (1.2, 9.4, 3.2)
    for row in table.rows:
        for i, w in enumerate(widths):
            row.cells[i].width = Cm(w)

    doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("Prepared for: Course Lecturer, CSC584 Enterprise Programming\n")
    r.font.size = Pt(9.5)
    r.font.color.rgb = SLATE
    r = p.add_run("Semester October 2026 – February 2027\n")
    r.font.size = Pt(9.5)
    r.font.color.rgb = SLATE
    r = p.add_run("Submission date: 16 November 2026")
    r.font.size = Pt(9.5)
    r.font.color.rgb = SLATE


def declaration(doc):
    page_break(doc)
    h1(doc, "Group Declaration")
    para(doc,
         "This proposal is our own group work for CSC584 Enterprise Programming. We designed and "
         "built the system described here, tested it together, and acknowledged every source, library "
         "and reference we used. The table below lists the group members and their student IDs.")
    doc.add_paragraph()
    table = doc.add_table(rows=1, cols=4)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    table.autofit = False
    set_table_borders(table)
    for i, text in enumerate(("No.", "Name", "Student ID", "Signature")):
        cell = table.rows[0].cells[i]
        cell.text = ""
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(9)
        run.font.color.rgb = WHITE
        set_cell_bg(cell, "0B1220")
        set_cell_margins(cell, 70, 70)
    for i, (sid, name) in enumerate(MEMBERS, start=1):
        cells = table.add_row().cells
        keep_row_together(table.rows[-1])
        for j, value in enumerate((str(i), name, sid, "")):
            cell = cells[j]
            cell.text = ""
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER if j != 1 else WD_ALIGN_PARAGRAPH.LEFT
            run = p.add_run(value)
            run.font.size = Pt(8.8)
            set_cell_margins(cell, 120, 120)
    for row in table.rows:
        for i, w in enumerate((1.1, 7.0, 3.0, 4.7)):
            row.cells[i].width = Cm(w)
    doc.add_paragraph()
    para(doc, "Declared on: 16 November 2026", size=9.5, color=SLATE)

    h2(doc, "Contents")
    toc = [
        ("1.0", "Project Overview"),
        ("2.0", "Problem Statement"),
        ("3.0", "Objective"),
        ("4.0", "Proposed User Interface"),
        ("5.0", "Database Design"),
        ("6.0", "Flow of Application"),
        ("7.0", "Conclusion"),
        ("A", "Appendix A — Demonstration Accounts and Setup"),
        ("B", "Appendix B — Project Structure"),
        ("C", "Appendix C — Rubric Traceability Matrix"),
    ]
    for num, title in toc:
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(3)
        r = p.add_run(f"{num}   ")
        r.bold = True
        r.font.color.rgb = GREEN
        r = p.add_run(title)
        r.font.size = Pt(10.5)


def overview(doc):
    page_break(doc)
    h1(doc, "1.0  Project Overview")

    h2(doc, "1.1  Introduction")
    para(doc,
         "Road races have become a regular weekend habit in Malaysia. Clubs, charities and commercial "
         "organisers put on everything from 5 km fun runs to full marathons, and thousands of people "
         "sign up every month. Yet many events are still run from a mix of online forms, spreadsheets, "
         "bank-transfer screenshots and messaging groups. That works until the entries start to pile "
         "up. Then the same runner appears twice, a payment cannot be matched to an entry, and nobody "
         "is sure which copy of the participant list is the latest.")
    para(doc,
         "RunEase puts that whole process in one place. Runners can browse approved races, compare "
         "categories, register and pay, then keep their bib and entry details in a single dashboard. "
         "Organisers can create an event, set categories and quotas, send it for approval, check "
         "runners in on race day and record results. Administrators verify organisers, review submitted "
         "events, manage accounts and go through the audit trail when something goes wrong.")
    para(doc,
         "We built the system as a Java EE web application that follows the Model-View-Controller "
         "(MVC) pattern. Servlets handle requests, JavaBeans hold the data, JSP and JSTL draw the pages, "
         "and JDBC talks to a relational database. The interface is responsive, so the same screens "
         "work on a laptop at race headquarters and on a phone at the starting line.")

    h2(doc, "1.2  Purpose and Scope")
    para(doc,
         "RunEase is meant to replace that patchwork with one system that knows who is using it and "
         "what they are allowed to do. The proposal covers the following:")
    bullets(doc, [
        "A public catalogue of approved events, with search, filters, sorting and pagination.",
        "Registration that lets a runner choose a category, jersey size, emergency contact and payment "
        "method, and returns a unique registration code and bib number.",
        "A participant area for viewing entries, paying for a reserved slot, cancelling an entry and "
        "printing race details.",
        "Organiser tools for drafting events, managing categories and quotas, submitting for approval, "
        "checking participants in, recording results, posting announcements and exporting CSV reports.",
        "Admin tools for verifying organisers, approving or rejecting events, managing user accounts, "
        "reviewing audit entries and producing platform reports.",
        "Security measures: authentication, role-based access control, password hashing, CSRF "
        "protection, security headers, session timeout and audit logging.",
    ])
    para(doc,
         "Several things are outside this academic build on purpose: real payment settlement, refunds "
         "to bank accounts, native mobile apps, chip-timing hardware and email or SMS delivery. "
         "Payments are simulated so the full registration lifecycle can be demonstrated safely, and "
         "the code is arranged so a real gateway could be plugged in later.")

    h2(doc, "1.3  Target Users and Their Needs")
    data_table(doc,
               ("User group", "Primary needs", "RunEase response"),
               [
                   ("Visitor", "Find races, compare distance, date, location and price without creating an account.",
                    "Public landing page and searchable catalogue of approved events."),
                   ("Participant", "Register quickly, pay securely, keep proof of entry and manage changes.",
                    "Participant dashboard, guided checkout, registration detail, bib card and self-service cancellation."),
                   ("Organiser", "Publish events, control quotas, manage participants and report outcomes.",
                    "Organiser dashboard, event builder, approval workflow, check-in, results and CSV reports."),
                   ("Administrator", "Govern the platform, verify organisers and investigate problems.",
                    "Admin dashboard, organiser verification queue, event review, user management and audit trail."),
               ],
               widths=(3.1, 7.0, 6.2))

    h2(doc, "1.4  System Modules")
    modules = [
        ("Public portal", "Landing page, event catalogue (search, type, state, date and price filters), event detail, "
                          "announcements and registration status."),
        ("Identity and access", "Registration and login for participants and organisers, BCrypt password hashing, "
                                "session rotation on login, role guards and account status checks."),
        ("Registration and checkout", "Category availability, quota checks, jersey and emergency details, payment "
                                      "method, immediate payment or reservation, unique code and bib assignment."),
        ("Participant workspace", "Dashboard summary, registration list with filters and sorting, registration detail, "
                                  "pay-later, cancellation and profile management."),
        ("Organiser workspace", "Event CRUD with pre-filled edit forms, category management, submit for approval, "
                                "participant filtering and check-in, result entry with rankings, announcements and reports."),
        ("Administration", "Organiser verification, event approval and rejection with reasons, asynchronous user "
                           "management, platform statistics and audit log review."),
        ("Reporting and audit", "Paginated, searchable, sortable and exportable reports; every state change is written "
                                "to an audit log with actor, IP address and timestamp."),
    ]
    data_table(doc, ("Module", "Responsibilities"), modules, widths=(3.6, 12.7))

    h2(doc, "1.5  Technology Stack")
    data_table(doc,
               ("Layer", "Technology", "Why it is used"),
               [
                   ("Language", "Java 21", "Modern language features and long-term support."),
                   ("Web API", "Jakarta EE 10: Servlet 6.0, JSP 3.1, JSTL 3.0", "Course-aligned server-side MVC stack."),
                   ("Persistence", "JDBC with DAO classes; H2 (MySQL mode) by default, MySQL optional",
                    "Direct SQL practice, portable schema and zero-setup demonstration."),
                   ("Build and packaging", "Maven WAR with embedded Tomcat 10.1 runner",
                    "Standard enterprise packaging plus a one-command local demo."),
                   ("View layer", "JSP, JSTL, HTML5, Bootstrap 5.3, custom CSS design system",
                    "Server-rendered pages with a responsive, accessible interface."),
                   ("Client behaviour", "Vanilla JavaScript modules (fetch, modals, toasts, validation)",
                    "Asynchronous CRUD and progressive enhancement without a heavy framework."),
                   ("Security", "BCrypt, CSRF synchroniser token, security headers, role filter",
                    "Defence in depth for session-based enterprise applications."),
                   ("Testing", "JUnit-ready service design, curl-based end-to-end verification",
                    "Traceable verification of every role and workflow."),
               ],
               widths=(2.9, 6.6, 6.8))

    h2(doc, "1.6  Rubric Alignment at a Glance")
    para(doc,
         "We planned the build around the assessment criteria, so each requirement can be shown in the "
         "running system rather than only described in this report.")
    data_table(doc,
               ("Rubric area", "How RunEase implements it"),
               [
                   ("Dual-layer validation", "HTML5 constraints in the browser plus a server-side Validation utility; "
                                             "database CHECK constraints provide a final safety net."),
                   ("RBAC and session security", "AuthFilter, per-request role checks and ownership checks; 30-minute "
                                                 "session timeout, HttpOnly cookie and session ID rotation on login."),
                   ("Asynchronous create and read", "Admin user management uses a JSON API and fetch to create, list and "
                                                    "search accounts without a full page reload."),
                   ("Update with pre-filled forms and modals", "Edit actions open a modal whose fields are populated from "
                                                               "data-* attributes and whose action is swapped dynamically."),
                   ("Safe delete with confirmation", "Deletion always requires an explicit confirmation dialog; events and "
                                                     "accounts use soft deletes and dependency checks."),
                   ("Search, sort, filter and pagination", "Every listing uses a reusable Page<T> object that preserves query "
                                                           "state across page and sort links."),
                   ("Reports and export", "Organiser and admin reports support date, event and status filters plus CSV export."),
                   ("Responsive modern UI", "Custom design system with navy and lime palette, responsive grids and a "
                                            "mobile navigation shell."),
                   ("Consistent navigation", "Three layout shells (public, auth, app) with breadcrumbs, active menu state and "
                                             "role-specific sidebars."),
                   ("Graceful error handling", "AppException produces branded 403, 404 and 500 pages; field errors, flash "
                                               "messages and toasts keep users informed."),
               ],
               widths=(5.1, 11.2))


def problem(doc):
    page_break(doc)
    h1(doc, "2.0  Problem Statement")

    h2(doc, "2.1  Current Practice")
    para(doc,
         "Ask a small race organiser how entries are handled and the answer usually sounds the same: "
         "a form builder for sign-ups, a shared drive for payment screenshots, a spreadsheet for "
         "participants and a printed list for check-in. Announcements go out on social media or in a "
         "messaging group, and results are posted as a PDF after someone has keyed them in.")
    para(doc,
         "This is workable for a few hundred runners. Beyond that, the cracks show. Duplicate entries "
         "creep in, payments do not match names, a category sells past its quota, and nobody can say "
         "who edited a record or when. The last week before the event turns into data cleaning instead "
         "of race preparation, and runners who cannot find their entry lose trust in the organiser.")

    h2(doc, "2.2  Identified Problems and Impact")
    data_table(doc,
               ("No.", "Problem", "Impact on organisers and participants"),
               [
                   ("P1", "Registration, payment and participant data live in separate files and channels.",
                    "Duplicate or missing entries; hours of manual reconciliation; no single source of truth."),
                   ("P2", "Payments are matched manually against bank transfers and screenshots.",
                    "Delayed confirmation, unpaid runners holding slots, and difficult refund decisions."),
                   ("P3", "Category quotas are tracked by hand.",
                    "Overselling, unfair start-line experiences and manual waiting lists."),
                   ("P4", "Anyone with the spreadsheet link can view or edit personal data.",
                    "Privacy exposure and no accountability for changes."),
                   ("P5", "Forms accept incomplete or invalid emergency and medical information.",
                    "Safety risk on race day and extra follow-up work for the organiser."),
                   ("P6", "Check-in and results are recorded on paper or ad-hoc sheets.",
                    "Long queues, transcription errors and delayed official results."),
                   ("P7", "Reports require copying data into spreadsheets and rebuilding formulas.",
                    "Slow decisions, inconsistent numbers and no repeatable export."),
                   ("P8", "Event approval and organiser verification are handled through chat messages.",
                    "No audit trail, unclear responsibility and inconsistent quality across events."),
               ],
               widths=(1.2, 7.4, 7.7))

    h2(doc, "2.3  Proposed Solution")
    para(doc,
         "Each problem below has a matching feature in RunEase and a way to demonstrate it. The table "
         "connects the three.")
    data_table(doc,
               ("Problem", "RunEase capability", "Evidence"),
               [
                   ("P1", "One relational database with users, events, categories, registrations, payments and results.",
                    "Registration code and bib are generated once and shown to both participant and organiser."),
                   ("P2", "Payment records linked one-to-one with registrations and a clear PAID / PENDING / REFUNDED state.",
                    "Participant can pay a reserved entry; organiser sees payment status in reports."),
                   ("P3", "Category quota enforced inside a database transaction with row locking.",
                    "Checkout rejects a full category and shows a friendly message."),
                   ("P4", "Authentication, role-based access control and ownership checks on every sensitive action.",
                    "A participant cannot open another runner's registration or an admin page."),
                   ("P5", "Dual-layer validation for contact, emergency, medical, date and payment fields.",
                    "Browser hints plus server-side errors and database CHECK constraints."),
                   ("P6", "Check-in action and result entry with automatic overall and category rankings.",
                    "Organiser participant list and results page update immediately."),
                   ("P7", "Reusable pagination, search, sort and filter objects plus CSV export.",
                    "Every report can be narrowed, ordered and exported without spreadsheets."),
                   ("P8", "Explicit approval workflow with reasons and a complete audit trail.",
                    "Admin review page, organiser rejection reason and audit log entries."),
               ],
               widths=(1.8, 8.2, 6.3))

    h2(doc, "2.4  Feasibility")
    bullets(doc, [
        "Technical: everything uses Java EE topics from CSC584 and runs on a standard servlet "
        "container. H2 gives us a zero-setup database for the demo, and the same schema can be moved "
        "to MySQL.",
        "Operational: the screens use patterns people already know — search boxes, filters, modals, "
        "confirmation prompts and dashboards — so organisers do not need special training.",
        "Economic: the stack is open source and can run on a low-cost server. The bigger saving is the "
        "manual reconciliation it removes and the registration mistakes it prevents.",
        "Schedule: the project ships as one Maven WAR with seed data, demo accounts and a documented "
        "run command, so demonstrations and marking stay predictable.",
    ])


def objectives(doc):
    page_break(doc)
    h1(doc, "3.0  Objective")

    h2(doc, "3.1  General Objective")
    para(doc,
         "To design and build a secure, responsive Java EE web application that handles the full "
         "lifecycle of running events for participants, organisers and administrators.")

    h2(doc, "3.2  Specific Objectives")
    numbered(doc, [
        "To publish a searchable catalogue of approved running events, including category, location, "
        "date and price.",
        "To run a guided registration and checkout flow that checks participant details, enforces "
        "category quotas and issues a unique registration code and bib number.",
        "To let participants pay for a reserved entry, cancel an entry and update their profile without "
        "contacting the organiser.",
        "To give organisers one workspace for drafting events, managing categories, submitting for "
        "approval, checking runners in, recording results and posting announcements.",
        "To give administrators the tools to verify organisers, review events, manage accounts, run "
        "reports and investigate activity through the audit log.",
        "To apply enterprise security practices: password hashing, session management, role-based "
        "access control, CSRF protection, security headers and audit logging.",
        "To keep the interface consistent and responsive, with breadcrumbs, clear error messages and "
        "confirmation before anything destructive happens.",
    ])

    h2(doc, "3.3  Success Criteria")
    data_table(doc,
               ("Criterion", "Target"),
               [
                   ("Role separation", "Every route is reachable only by its permitted role; unauthorised access returns 403."),
                   ("Validation", "Invalid input is rejected by the browser and the server, with a readable field or form message."),
                   ("Registration integrity", "No duplicate active entry per participant per event; no registration beyond category quota."),
                   ("Auditability", "Login, create, update, delete, approve, reject, check-in and export actions appear in the audit log."),
                   ("Usability", "A participant can complete registration in under three minutes on a phone-sized viewport."),
                   ("Reporting", "Every listing can be searched, filtered, sorted and paged without losing query state."),
                   ("Reliability", "Branded 403, 404 and 500 pages replace stack traces; the application starts with one command."),
               ],
               widths=(4.0, 12.3))


SCREENS = [
    ("01-home.jpeg", "Figure 4.1 — Public landing page",
     "The landing page explains what RunEase does, points visitors to the three role journeys and shows "
     "approved events. It uses the same navigation and visual style as the signed-in application."),
    ("02-catalog.jpeg", "Figure 4.2 — Event catalogue with search, filters, sorting and pagination",
     "Visitors can search by keyword and narrow the list by type, state and date. Sort and page links "
     "keep the current query string, and each card shows what a runner needs to decide: date, location, "
     "distance and price."),
    ("04-event-detail.jpeg", "Figure 4.3 — Event detail and race categories",
     "The event page carries the race description, schedule, venue and category table. Each category "
     "shows whether it is open, full or not yet open, so nobody clicks a button that cannot work."),
    ("03-participant-dashboard.jpeg", "Figure 4.4 — Participant dashboard",
     "This is the runner's home page. It summarises upcoming races, confirmed entries, money spent and "
     "recent activity, and links straight to payments, bib details and cancellations."),
    ("05-checkout.jpeg", "Figure 4.5 — Checkout with dual-layer validation",
     "Checkout asks for jersey size, emergency contact, medical notes and payment method. Required "
     "fields are marked, the browser gives immediate feedback and the server repeats every check before "
     "it creates the registration and payment."),
    ("06-registration-detail.jpeg", "Figure 4.6 — Registration detail, payment and bib card",
     "The registration page is the runner's proof of entry: code, status, category, bib number, payment "
     "method, emergency and medical notes. From here they can pay later or cancel after confirming."),
    ("07-organizer-dashboard.jpeg", "Figure 4.7 — Organiser dashboard",
     "The organiser dashboard pulls together event status, registration trends, revenue and race-day "
     "readiness. Each card links to the next job, such as reviewing a draft or checking runners in."),
    ("08-organizer-events.jpeg", "Figure 4.8 — Organiser event list",
     "Events are listed in a table that can be searched, filtered and sorted, with status pills and "
     "actions for each row. Deletions ask for confirmation and edit forms open already filled in."),
    ("09-organizer-participants.jpeg", "Figure 4.9 — Participant management and check-in",
     "Organisers can filter runners by event, category and status, then check someone in with one "
     "confirmed action. The audit log records who did it and from which IP address."),
    ("10-organizer-reports.jpeg", "Figure 4.10 — Reports with filters and CSV export",
     "Reports show live totals for registrations, revenue and attendance. The filter bar drives both "
     "the table and the CSV export, so the downloaded file matches what is on screen."),
    ("11-admin-dashboard.jpeg", "Figure 4.11 — Administrator dashboard",
     "The admin dashboard gives a platform-wide view of users, organisers, events, registrations, "
     "revenue and pending approvals, with links into each review queue."),
    ("12-admin-events.jpeg", "Figure 4.12 — Event review and approval",
     "Admins read a submitted event in context, then approve it for the catalogue or reject it with a "
     "reason. Both the decision and the reason are stored and shown to the organiser."),
    ("13-admin-users.jpeg", "Figure 4.13 — Asynchronous user management",
     "The user page loads and changes accounts through a JSON API. Create and edit use a modal, "
     "validation errors come back as 422 responses and status changes happen without a page reload."),
    ("14-admin-audit.jpeg", "Figure 4.14 — Audit trail",
     "Every sensitive action is stored with the actor, action, entity, description, IP address and "
     "time. Filters and paging make it practical to reconstruct an incident."),
]


def interface(doc):
    page_break(doc)
    h1(doc, "4.0  Proposed User Interface")

    h2(doc, "4.1  Design Direction")
    para(doc,
         "We kept the visual design simple on purpose: dark navy surfaces, a lime accent for primary "
         "actions and status highlights, plenty of white space and a clear type hierarchy. The same "
         "interface has to work in two very different places — a runner scrolling on a phone and an "
         "organiser working through a dense table on a laptop.")
    bullets(doc, [
        "Colour: navy #0B1220 for structure, lime #C9F04C for actions and status, slate #5C677D for "
        "secondary text, and a soft paper background behind data-heavy screens.",
        "Type: Inter for interface text and Space Grotesk for display headings.",
        "Layout: responsive CSS grid and flexbox, a collapsible sidebar on small screens and a sticky "
        "top bar.",
        "Components: cards, status pills, timelines, tabs, filter bars, data tables, modals, toasts and "
        "empty states.",
        "Accessibility: semantic landmarks, visible focus styles, labelled controls, a skip link, ARIA "
        "live regions for flash messages and colour combinations that pass contrast checks.",
    ])

    h2(doc, "4.2  Navigation and Information Architecture")
    data_table(doc,
               ("Area", "Main navigation", "Breadcrumb pattern"),
               [
                   ("Public", "Home · Events · Sign in · Create account", "Home / Events / Event title"),
                   ("Participant", "Dashboard · Events · My registrations · Profile", "My registrations / Registration code"),
                   ("Organiser", "Dashboard · My events · Participants · Results · Announcements · Reports",
                    "My events / Event title / Manage event"),
                   ("Administrator", "Dashboard · Events · Users · Organisers · Reports · Audit",
                    "Admin / Events / Event review"),
               ],
               widths=(2.6, 8.8, 4.9))
    para(doc,
         "Every signed-in page uses the same shell: a sidebar that changes with the role, a top bar "
         "with search and the account menu, a breadcrumb trail and a space for flash messages. That "
         "consistency matters when one person holds more than one role, and when a link from a report "
         "has to make sense after it is opened.")

    h2(doc, "4.3  Proposed Screens")
    para(doc,
         "These screenshots come from the working system. Between them they cover the public site, the "
         "participant journey, the organiser workspace and the admin area — well beyond the five "
         "screens the brief asks for.")

    for i, (filename, cap, text) in enumerate(SCREENS):
        path = SHOTS / filename
        if not path.exists():
            continue
        h3(doc, cap.replace("Figure 4.", "4.3.").split(" — ")[0] + "  " + cap.split(" — ")[1])
        width = 6.0
        if filename == "05-checkout.jpeg":
            width = 6.2
        figure(doc, path, cap, width=width)
        para(doc, text, size=9.5)
        if i in (2, 5, 9):
            page_break(doc)

    h2(doc, "4.4  Interaction Patterns")
    data_table(doc,
               ("Pattern", "Implementation"),
               [
                   ("Asynchronous create and read", "The admin user list is rendered once; users.js calls "
                                                    "GET /api/admin/users with search, role, status and page parameters "
                                                    "and updates the table without reloading."),
                   ("Modal editing with pre-filled fields", "Edit buttons carry data-field-* and data-form-action "
                                                            "attributes. The shared modal copies those values into its "
                                                            "inputs and changes its title and action."),
                   ("Safe destructive actions", "Forms with data-confirm open a shared confirmation modal. The server "
                                                "also enforces ownership and dependency rules; events and accounts are "
                                                "soft-deleted so history remains auditable."),
                   ("Progressive feedback", "Flash messages survive redirects, toasts confirm asynchronous changes, and "
                                            "field-level errors are attached to the exact input that failed."),
                   ("State-preserving pagination", "Page<T> builds page and sort URLs from the current query string, so "
                                                   "filters and search terms survive navigation."),
                   ("Empty and error states", "Empty tables explain what to do next; branded 403, 404 and 500 pages "
                                              "provide a route back to safety."),
               ],
               widths=(4.7, 11.6))

    h2(doc, "4.5  Responsive Behaviour")
    para(doc,
         "We tested the layout from 360 px phone widths up to desktop. Wide tables scroll horizontally "
         "instead of breaking the page, filter bars wrap, the sidebar turns into an off-canvas drawer "
         "and primary actions stay within thumb reach. The same templates are reused across roles, so "
         "the behaviour stays predictable.")


DDL = {
    "users": [
        ("user_id", "BIGINT", "PK, auto-increment"),
        ("full_name", "VARCHAR(120)", "Not null"),
        ("email", "VARCHAR(160)", "Not null, unique, login identity"),
        ("password_hash", "VARCHAR(100)", "Not null, BCrypt hash"),
        ("phone", "VARCHAR(20)", "Optional contact number"),
        ("role", "VARCHAR(20)", "ADMIN, ORGANIZER or PARTICIPANT"),
        ("status", "VARCHAR(20)", "ACTIVE, INACTIVE or SUSPENDED"),
        ("gender", "VARCHAR(10)", "Optional: MALE or FEMALE"),
        ("date_of_birth", "DATE", "Optional date of birth"),
        ("city", "VARCHAR(80)", "Optional city"),
        ("last_login_at", "TIMESTAMP", "Updated on successful login"),
        ("created_at", "TIMESTAMP", "Default current timestamp"),
        ("updated_at", "TIMESTAMP", "Updated on profile or status change"),
        ("deleted_at", "TIMESTAMP", "Soft-delete marker"),
    ],
    "organizers": [
        ("organizer_id", "BIGINT", "PK, auto-increment"),
        ("user_id", "BIGINT", "FK to users, unique; one profile per organiser account"),
        ("organization_name", "VARCHAR(150)", "Not null"),
        ("registration_no", "VARCHAR(50)", "Business registration number"),
        ("contact_person", "VARCHAR(120)", "Operational contact"),
        ("website", "VARCHAR(160)", "Optional organisation website"),
        ("verification_status", "VARCHAR(20)", "PENDING, VERIFIED or REJECTED"),
        ("verified_at", "TIMESTAMP", "When the decision was made"),
        ("verified_by", "BIGINT", "FK to users; administrator who decided"),
        ("created_at", "TIMESTAMP", "Default current timestamp"),
    ],
    "events": [
        ("event_id", "BIGINT", "PK, auto-increment"),
        ("organizer_id", "BIGINT", "FK to organizers"),
        ("title", "VARCHAR(150)", "Not null"),
        ("slug", "VARCHAR(170)", "Not null, unique public identifier"),
        ("description", "TEXT", "Race story, schedule and rules"),
        ("event_type", "VARCHAR(20)", "ROAD, TRAIL, FUN_RUN or VIRTUAL"),
        ("venue", "VARCHAR(150)", "Not null"),
        ("city / state", "VARCHAR(80)", "Location details"),
        ("event_date", "DATE", "Not null"),
        ("flag_off_time", "TIME", "Optional start time"),
        ("registration_open", "DATE", "Not null"),
        ("registration_close", "DATE", "Not null; must be on or before event date"),
        ("status", "VARCHAR(20)", "DRAFT, PENDING, APPROVED, REJECTED, COMPLETED or CANCELLED"),
        ("rejection_reason", "VARCHAR(255)", "Administrator explanation"),
        ("created_at / updated_at", "TIMESTAMP", "Lifecycle timestamps"),
        ("deleted_at", "TIMESTAMP", "Soft-delete marker"),
    ],
    "event_categories": [
        ("category_id", "BIGINT", "PK, auto-increment"),
        ("event_id", "BIGINT", "FK to events, cascade delete"),
        ("name", "VARCHAR(60)", "Unique per event"),
        ("distance_km", "DECIMAL(5,1)", "Positive distance"),
        ("price", "DECIMAL(10,2)", "Non-negative entry fee"),
        ("quota", "INT", "Positive participant limit"),
        ("start_time", "TIME", "Optional category start time"),
        ("created_at / updated_at", "TIMESTAMP", "Lifecycle timestamps"),
    ],
    "registrations": [
        ("registration_id", "BIGINT", "PK, auto-increment"),
        ("registration_code", "VARCHAR(20)", "Not null, unique public reference"),
        ("user_id", "BIGINT", "FK to users"),
        ("category_id", "BIGINT", "FK to event_categories"),
        ("bib_number", "VARCHAR(15)", "Unique when assigned"),
        ("jersey_size", "VARCHAR(5)", "XS, S, M, L, XL or XXL"),
        ("emergency_name", "VARCHAR(120)", "Not null"),
        ("emergency_phone", "VARCHAR(20)", "Not null"),
        ("medical_notes", "VARCHAR(255)", "Optional but visible to the organiser"),
        ("status", "VARCHAR(20)", "PENDING_PAYMENT, CONFIRMED, CANCELLED, CHECKED_IN, FINISHED or DISQUALIFIED"),
        ("total_amount", "DECIMAL(10,2)", "Amount charged"),
        ("registered_at", "TIMESTAMP", "Default current timestamp"),
        ("checked_in_at", "TIMESTAMP", "Race-day check-in time"),
        ("cancelled_at", "TIMESTAMP", "Cancellation time"),
        ("deleted_at", "TIMESTAMP", "Soft-delete marker"),
    ],
    "payments": [
        ("payment_id", "BIGINT", "PK, auto-increment"),
        ("registration_id", "BIGINT", "FK to registrations, unique; one payment per entry"),
        ("reference_no", "VARCHAR(40)", "Not null, unique"),
        ("method", "VARCHAR(20)", "FPX, CARD or EWALLET"),
        ("amount", "DECIMAL(10,2)", "Amount charged"),
        ("status", "VARCHAR(20)", "PENDING, PAID, REFUNDED or FAILED"),
        ("paid_at", "TIMESTAMP", "Payment confirmation time"),
        ("created_at", "TIMESTAMP", "Default current timestamp"),
    ],
    "results": [
        ("result_id", "BIGINT", "PK, auto-increment"),
        ("registration_id", "BIGINT", "FK to registrations, unique; one result per entry"),
        ("finish_time_seconds", "INT", "Positive finish time in seconds"),
        ("overall_position", "INT", "Recalculated after every saved result"),
        ("category_position", "INT", "Recalculated within the race category"),
        ("pace_seconds", "INT", "Average pace per kilometre"),
        ("recorded_by", "BIGINT", "FK to users; organiser who recorded the result"),
        ("recorded_at", "TIMESTAMP", "Default current timestamp"),
    ],
    "announcements": [
        ("announcement_id", "BIGINT", "PK, auto-increment"),
        ("event_id", "BIGINT", "FK to events, cascade delete"),
        ("title", "VARCHAR(150)", "Not null"),
        ("body", "TEXT", "Not null"),
        ("audience", "VARCHAR(20)", "ALL or CONFIRMED"),
        ("created_by", "BIGINT", "FK to users"),
        ("published_at", "TIMESTAMP", "Default current timestamp"),
        ("updated_at / deleted_at", "TIMESTAMP", "Edit and soft-delete markers"),
    ],
    "audit_logs": [
        ("log_id", "BIGINT", "PK, auto-increment"),
        ("user_id", "BIGINT", "FK to users; nullable for system events"),
        ("action", "VARCHAR(30)", "LOGIN, CREATE, UPDATE, DELETE, APPROVE, REJECT, CHECK_IN, EXPORT"),
        ("entity_type", "VARCHAR(40)", "USER, EVENT, REGISTRATION, RESULT, ANNOUNCEMENT, REPORT"),
        ("entity_id", "VARCHAR(40)", "Affected record identifier"),
        ("description", "VARCHAR(255)", "Human-readable change summary"),
        ("ip_address", "VARCHAR(45)", "Client address"),
        ("created_at", "TIMESTAMP", "Default current timestamp"),
    ],
}

TABLE_PURPOSE = [
    ("users", "Identity and access", "Stores every account, role and account status."),
    ("organizers", "Organiser profile", "Extends an ORGANIZER user with organisation and verification data."),
    ("events", "Event lifecycle", "Stores event details and the DRAFT → PENDING → APPROVED/REJECTED workflow."),
    ("event_categories", "Race inventory", "Defines distance, price, quota and start time per event."),
    ("registrations", "Participation", "Links a participant to a category and records bib, emergency and status data."),
    ("payments", "Financial record", "One-to-one payment state for a registration."),
    ("results", "Race outcomes", "Finish time, positions and pace for a checked-in registration."),
    ("announcements", "Communication", "Organiser updates targeted at all or confirmed participants."),
    ("audit_logs", "Accountability", "Immutable record of sensitive actions with actor and IP address."),
]


def database(doc):
    page_break(doc)
    h1(doc, "5.0  Database Design")

    h2(doc, "5.1  Design Approach")
    para(doc,
         "The schema is in third normal form and is built around four questions: who is acting, which "
         "event are they running, what entry did they buy, and what happened on race day. Each table has "
         "one job, and every relationship is held together by a foreign key. Status values are also "
         "guarded by CHECK constraints, so a bad value cannot slip in even if the application "
         "validation is bypassed.")
    bullets(doc, [
        "Normalisation: things like category names and event titles are stored once and referenced by key.",
        "Referential integrity: foreign keys protect the links between organisers, categories, "
        "registrations, payments and results.",
        "Business rules: registration codes and bibs are unique, each registration has one payment and at "
        "most one result, a runner cannot hold two active entries in the same event, and quotas, prices "
        "and finish times must be positive.",
        "Soft deletion: events, users, announcements and registrations keep a deleted_at marker, so "
        "history and audit references still make sense.",
        "Indexes: role and status, event date, category status, registration date, payment status, finish "
        "time, announcement event and audit timestamp.",
        "Transactions: quota checks, registration creation, payment confirmation, approval and result "
        "ranking all run inside database transactions.",
    ])

    h2(doc, "5.2  Table Summary")
    data_table(doc,
               ("Table", "Responsibility", "Purpose"),
               TABLE_PURPOSE,
               widths=(3.1, 3.7, 9.5))

    h2(doc, "5.3  Data Dictionary")
    para(doc,
         "The tables below describe the physical schema. Types are written in MySQL/H2 form; both "
         "databases use the same definitions.")
    for index, (table, responsibility, purpose) in enumerate(TABLE_PURPOSE, start=1):
        h3(doc, f"5.3.{index}  {table}")
        para(doc, purpose, size=9.5, color=SLATE, space_after=3)
        data_table(doc, ("Column", "Type", "Constraints and description"), DDL[table],
                   widths=(4.2, 3.7, 8.4), font=8.3)

    # Landscape section for the ERD
    section = doc.add_section(WD_SECTION.NEW_PAGE)
    landscape(section)
    h2(doc, "5.4  Entity Relationship Diagram")
    para(doc,
         "The ERD shows the nine tables and how they connect. Solid lines are the main transaction "
         "paths, while dashed lines are secondary references such as the admin who verified an "
         "organiser or the organiser who recorded a result.", size=9.5)
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(str(IMAGES / "erd.png"), width=Inches(8.0))
    caption(doc, "Figure 5.1 — RunEase entity relationship diagram")

    section = doc.add_section(WD_SECTION.NEW_PAGE)
    page_setup(section)
    h2(doc, "5.5  Relationship Cardinality")
    data_table(doc,
               ("Parent", "Child", "Cardinality", "Rule"),
               [
                   ("users", "organizers", "1 : 0..1", "An organiser account has one organisation profile."),
                   ("organizers", "events", "1 : N", "An organiser can create many events."),
                   ("events", "event_categories", "1 : N", "An event has one or more race categories."),
                   ("event_categories", "registrations", "1 : N", "A category holds many registrations."),
                   ("users", "registrations", "1 : N", "A participant can register for many events."),
                   ("registrations", "payments", "1 : 1", "Each registration has exactly one payment record."),
                   ("registrations", "results", "1 : 0..1", "A checked-in registration may have one result."),
                   ("events", "announcements", "1 : N", "An event can publish many announcements."),
                   ("users", "announcements", "1 : N", "An organiser can author many announcements."),
                   ("users", "audit_logs", "1 : N", "A user can produce many audit entries."),
                   ("users", "results", "1 : N", "An organiser can record many results."),
                   ("users", "organizers", "1 : N", "An administrator can verify many organisers."),
               ],
               widths=(2.8, 3.6, 2.6, 7.3), font=8.8)

    h2(doc, "5.6  Integrity and Security Notes")
    bullets(doc, [
        "Passwords are never stored in plain text; login compares against a BCrypt hash.",
        "Registration creation locks the category row before checking the quota, so two people cannot buy "
        "the last slot at the same time.",
        "The duplicate-entry check runs in the same transaction as registration creation, with a query "
        "that ignores cancelled entries.",
        "Unique constraints on registration_code, bib_number, reference_no and the payment/result links "
        "prevent duplicate records.",
        "Audit records are written in the same transaction as the change they describe, so an action "
        "cannot succeed without its audit entry.",
        "Database credentials and the JDBC URL come from environment variables rather than hard-coded "
        "production settings.",
    ])


FLOW_ROWS = [
    ("Visitor", "Open the public site, browse approved events, search, filter and sort, then open an event to "
                "compare categories and prices.", "No account is required; only APPROVED events are visible."),
    ("Authentication", "Register as a participant or organiser, then sign in. The server verifies the BCrypt "
                       "hash, account status and role, rotates the session and issues a CSRF token.",
     "Invalid credentials and suspended accounts receive a safe, generic error."),
    ("Participant", "Choose a category, complete checkout, pay now or reserve, receive a registration code and bib, "
                    "then manage the entry later.", "Quota, registration window and duplicate-entry rules are enforced."),
    ("Organiser", "Create an event draft, add categories and quotas, submit for approval, manage participants, "
                  "check runners in, record results and export reports.",
     "Only verified organisers can publish; only DRAFT or REJECTED events can be submitted."),
    ("Administrator", "Verify organisers, review event submissions, approve or reject with a reason, manage "
                      "accounts, inspect reports and audit activity.",
     "Every decision is written to the audit log with the administrator identity."),
    ("Error handling", "Validation failures return the form with field messages; authorisation failures return "
                       "403; missing records return 404; unexpected faults return a branded 500 page.",
     "No stack trace or database detail is exposed to the user."),
]


def flow(doc):
    section = doc.add_section(WD_SECTION.NEW_PAGE)
    landscape(section)
    h1(doc, "6.0  Flow of Application")

    h2(doc, "6.1  Application Flowchart")
    para(doc,
         "The flowchart keeps the public and login layers separate from the three role workspaces. Solid "
         "arrows are the main path; dashed arrows are handoffs between roles, such as an organiser "
         "submitting an event, an admin approving it, or a confirmed registration appearing in the "
         "organiser's participant list.")
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.keep_with_next = True
    p.add_run().add_picture(str(IMAGES / "flowchart.png"), width=Inches(8.0))
    caption(doc, "Figure 6.1 — RunEase application flow across visitor, participant, organiser and administrator roles")

    section = doc.add_section(WD_SECTION.NEW_PAGE)
    page_setup(section)
    h2(doc, "6.2  Flow Narrative")
    data_table(doc, ("Stage", "What happens", "Control"), FLOW_ROWS, widths=(2.8, 8.5, 5.0), font=8.7)

    para(doc,
         "The flow starts with public discovery, because most runners compare events before they create "
         "an account. Login is only required at the point of action: anyone can browse the catalogue and "
         "open an event, but checkout sends them to sign in and brings them back to the category they "
         "picked.")
    para(doc,
         "Once someone signs in, the role guard chooses the workspace. A participant lands on the runner "
         "dashboard, an organiser sees event and operational tools, and an admin sees the governance "
         "queues. Changing the URL does not cross those boundaries, because every controller checks the "
         "role and, for individual records, who owns them.")
    para(doc,
         "The organiser and admin flows meet at the approval boundary. An event stays a draft until the "
         "organiser adds at least one category and submits it. The admin can then approve it, which puts "
         "it in the catalogue, or reject it with a reason the organiser can act on. That gate stops "
         "incomplete or unverified events from taking money.")
    para(doc,
         "Registration itself is transactional. The system locks the category row, checks the quota and "
         "the runner's active entries, creates the registration and payment, assigns the next bib number "
         "when payment is confirmed, and writes an audit entry. If any step fails, the transaction rolls "
         "back and the runner gets an error they can fix, not a half-finished registration.")

    h2(doc, "6.3  Validation and Error Handling Flow")
    data_table(doc,
               ("Layer", "Mechanism", "User experience"),
               [
                   ("Browser", "HTML5 required, type, pattern, min, max and maxlength constraints plus custom "
                               "JavaScript form checks.", "Immediate field hints before the request is sent."),
                   ("Server", "Validation utility for required, length, name, phone, email, date, number, range and "
                              "enumerated values.", "Form is re-rendered with the submitted values and a field-level message."),
                   ("Domain", "Business rules in services: quota, registration window, duplicate entry, status "
                              "transitions, ownership and approval rules.", "Readable AppException message shown as a flash or form notice."),
                   ("Database", "Primary keys, foreign keys, unique constraints and CHECK constraints.", "Transaction rolls back; the service reports a safe message."),
                   ("Global", "BaseServlet maps AppException and unexpected exceptions to branded error pages.",
                    "403, 404 or 500 page with a route back to the dashboard or catalogue."),
               ],
               widths=(2.5, 8.7, 5.1), font=8.7)

    h2(doc, "6.4  Session and Security Flow")
    para(doc,
         "A successful login creates a new session ID, keeps only the user identity and role in the "
         "session, and issues a CSRF token. Every request that changes state has to present that token. "
         "The authentication filter checks the session and role before the controller runs, and the "
         "security headers filter adds CSP, frame, MIME-sniffing, referrer and permissions policies. "
         "Sensitive responses are marked no-store so personal data is not left in a shared browser "
         "cache.")


def conclusion(doc):
    page_break(doc)
    h1(doc, "7.0  Conclusion")
    para(doc,
         "RunEase shows how a well-structured Java EE application can solve a real operational problem "
         "without giving up security or usability. The proposal set out the gap between manual race "
         "administration and what participants, organisers and admins actually need, then matched each "
         "problem to something concrete in the system.")
    para(doc,
         "The build uses MVC and a normalised schema to keep business rules in one place. Validation at "
         "two layers, transactional registration, role-based access control, CSRF protection and audit "
         "logging make the results trustworthy. The design system stays consistent across all four "
         "journeys, and the reporting layer turns day-to-day data into decisions instead of "
         "spreadsheets.")
    para(doc,
         "The next step is to deploy the WAR to a shared servlet container, connect a production MySQL "
         "instance, swap the simulated payment adapter for a real gateway and add automated JUnit and "
         "browser tests. With those in place, RunEase could move from coursework to a service that race "
         "organisers actually use, without changing the architecture or the security model.")
    para(doc,
         "Overall, the project meets its objectives. It covers the full event lifecycle, protects each "
         "role's data, supports fast and accurate race-day work, and gives administrators the evidence "
         "they need to run the platform.")


def appendices(doc):
    page_break(doc)
    h1(doc, "Appendix A — Demonstration Accounts and Setup")
    para(doc,
         "The application ships with generated demo data: 20 users, 3 organisers, 10 events, "
         "59 registrations, 13 results and some audit history. The schema and seed load automatically "
         "on first start.")
    data_table(doc,
               ("Role", "Email", "Password", "Purpose"),
               [
                   ("Administrator", "admin@runease.my", "Password123!", "Governance, approvals, users, audit."),
                   ("Organiser", "ahmad@paceevents.my", "Password123!", "Pace Events; owns events 1, 2, 4, 7 and 9."),
                   ("Participant", "alia@student.uitm.edu.my", "Password123!", "Runner with several seeded entries."),
                   ("Participant", "kavin@runease.my", "Password123!", "Additional runner for registration tests."),
               ],
               widths=(2.8, 5.2, 3.0, 5.3))

    h2(doc, "A.1  Run Locally")
    para(doc, "Prerequisites: JDK 21 and the bundled Maven wrapper (or Maven 3.9 or newer).")
    for line in (
        ".\\run.ps1",
        "# open http://localhost:8080/",
        ".\\run.ps1 -Package",
        "# deploy target/runease.war to Tomcat 10.1+",
    ):
        p = doc.add_paragraph()
        p.paragraph_format.left_indent = Cm(0.6)
        p.paragraph_format.space_after = Pt(2)
        r = p.add_run(line)
        r.font.name = "Consolas"
        r.font.size = Pt(9)
        r.font.color.rgb = NAVY

    h2(doc, "A.2  Database Options")
    bullets(doc, [
        "Default: embedded H2 in MySQL compatibility mode, stored at ./data/runease; the schema and seed "
        "are applied automatically.",
        "MySQL: create the schema from database/schema_mysql.sql, load database/seed_mysql.sql, then set "
        "RUNEASE_DB_URL, RUNEASE_DB_USER and RUNEASE_DB_PASSWORD before starting the application.",
        "The WAR runs on any Jakarta EE 10 servlet container; the embedded Tomcat runner is only for "
        "demonstrations.",
    ])

    h1(doc, "Appendix B — Project Structure")
    structure = [
        ("src/main/java/my/uitm/runease/config", "Database connection, transaction and read helpers, schema bootstrap."),
        ("src/main/java/my/uitm/runease/model", "JavaBean entities and enums for users, events, categories, registration, payment, result, announcement and audit."),
        ("src/main/java/my/uitm/runease/dao", "JDBC data access objects with prepared statements and row mapping."),
        ("src/main/java/my/uitm/runease/service", "Business rules, transactions, validation orchestration and reporting."),
        ("src/main/java/my/uitm/runease/web", "Filters, BaseServlet, session helpers and route dispatch."),
        ("src/main/java/my/uitm/runease/web/servlet", "Public, authentication, participant, organiser, admin and API controllers."),
        ("src/main/webapp/WEB-INF/views", "JSP views grouped by role, shared layout fragments and reusable pagination."),
        ("src/main/webapp/assets", "Design system CSS, JavaScript modules, Bootstrap, icons and fonts."),
        ("src/main/resources/db", "H2/MySQL-compatible schema and generated seed data."),
        ("database", "Seed generator and MySQL schema/seed export."),
        ("docs/proposal", "This proposal, screenshots, diagrams and build scripts."),
    ]
    data_table(doc, ("Path", "Contents"), structure, widths=(6.2, 10.1), font=8.6)

    h1(doc, "Appendix C — Rubric Traceability Matrix")
    rows = [
        ("Dual-layer validation", "HTML5 constraints; Validation.java; CHECK constraints",
         "Register, login, checkout, event form, announcements, results."),
        ("RBAC and session security", "AuthFilter; role checks in BaseServlet/controllers; session rotation; 30-minute timeout",
         "Every /admin, /organizer, /checkout and /registrations route."),
        ("Async create/read", "GET/POST /api/admin/users; users.js fetch and table rendering",
         "Admin → Users."),
        ("Update with pre-filled forms and modals", "data-field-* attributes, shared modal, default action restoration",
         "Admin users; organiser events; announcements; results."),
        ("Safe delete with confirmation", "data-confirm modal; soft delete; dependency checks",
         "Events, users, announcements, registrations, results."),
        ("Search, sort, filter, pagination", "Page<T>; filter bars; preserved query strings",
         "Catalogue, registrations, organiser events/participants/results/reports, admin users/events/audit."),
        ("Reports and export", "ReportService; filtered CSV response",
         "Organiser reports; admin reports."),
        ("Responsive modern UI", "Custom design system; responsive shells; Bootstrap grid",
         "All screens from 360 px upward."),
        ("Consistent navigation", "Public/auth/app shells; breadcrumbs; active state",
         "All screens."),
        ("Graceful error handling", "AppException mapping; branded 403/404/500; flash and toasts",
         "All controllers and filters."),
    ]
    data_table(doc, ("Rubric area", "Implementation", "Demonstration"), rows,
               widths=(4.2, 7.3, 4.8), font=8.6)

    doc.add_paragraph()
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run("— End of proposal —")
    r.italic = True
    r.font.color.rgb = SLATE
    r.font.size = Pt(9)


def build():
    doc = Document()
    style_document(doc)
    section = doc.sections[0]
    page_setup(section)
    add_header_footer(section)
    section.different_first_page_header_footer = True

    cover(doc)
    declaration(doc)
    overview(doc)
    problem(doc)
    objectives(doc)
    interface(doc)
    database(doc)
    flow(doc)
    conclusion(doc)
    appendices(doc)

    # Section-aware header/footer for later sections (linked by default, but ensure page setup)
    for sec in doc.sections[1:]:
        if sec.orientation == WD_ORIENT.LANDSCAPE:
            landscape(sec)
        else:
            page_setup(sec)

    doc.core_properties.title = "RunEase — Running Event Management System"
    doc.core_properties.subject = "CSC584 Enterprise Programming Group Project Proposal"
    doc.core_properties.author = ", ".join(name.title() for _, name in MEMBERS)
    doc.core_properties.keywords = "Java EE, Servlet, JSP, JDBC, running event, registration, MVC"

    doc.save(OUT)
    print("Wrote", OUT)


if __name__ == "__main__":
    build()
