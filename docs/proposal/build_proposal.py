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
         "We declare that this proposal is the result of our own group work for CSC584 Enterprise "
         "Programming. All sources, libraries and reference materials used have been acknowledged. "
         "The system described in this proposal was designed, implemented and tested by the group "
         "members listed below.")
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
         "Running has become one of the fastest-growing mass-participation activities in Malaysia. "
         "Every weekend, thousands of runners register for road races, trail events, fun runs and "
         "virtual challenges organised by clubs, charities and commercial race organisers. Although "
         "demand is high, many organisers still depend on a combination of online forms, spreadsheets, "
         "bank-transfer screenshots and messaging groups to run an event. The result is duplicated "
         "entries, slow payment reconciliation, unclear cancellation policies and a poor experience "
         "for both runners and organisers.")
    para(doc,
         "RunEase is a web-based running event management system that brings the complete event "
         "lifecycle into one platform. Participants can discover approved races, compare categories, "
         "register, pay, receive a bib number and manage their entries. Organisers can draft an event, "
         "define race categories and quotas, submit it for approval, check runners in on race day, "
         "record finish times and export reports. Administrators verify organisers, review events, "
         "manage user accounts and audit every important action.")
    para(doc,
         "The system is implemented as a Java EE web application using the Model-View-Controller "
         "(MVC) pattern. Servlets act as controllers, JavaBeans model the domain, JSP and JSTL render "
         "the views, and JDBC with a relational database provides persistence. The interface uses a "
         "responsive, modern design system so that the same application works on a laptop at race "
         "headquarters and on a phone at the starting line.")

    h2(doc, "1.2  Purpose and Scope")
    para(doc,
         "The purpose of RunEase is to replace fragmented manual event administration with a single, "
         "role-aware system that is secure, auditable and easy to use. The scope of the proposal covers:")
    bullets(doc, [
        "A public catalogue of approved events with keyword search, filters, sorting and pagination.",
        "Participant registration with category selection, jersey size, emergency contact, payment "
        "method and a unique registration code and bib number.",
        "Participant self-service: view entries, pay a reserved entry, cancel an entry and download "
        "or print race details.",
        "Organiser workspaces for event drafting, category and quota management, approval submission, "
        "participant check-in, result recording, announcements and CSV reports.",
        "Administrator workspaces for organiser verification, event approval or rejection, user account "
        "management, audit review and platform reports.",
        "Security controls: authentication, role-based access control, password hashing, CSRF "
        "protection, security headers, session timeout and audit logging.",
    ])
    para(doc,
         "Out of scope for this academic implementation are real payment-gateway settlement, automated "
         "refunds to banks, native mobile applications, chip-timing hardware integration and email or "
         "SMS delivery. The payment flow is simulated so that the complete registration lifecycle can "
         "be demonstrated safely, while keeping the design ready for a real gateway adapter.")

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
         "The implementation was planned around the assessment criteria so that each requirement is "
         "visible in the running system, not only described in the report.")
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
         "A typical Malaysian running event today is managed through a temporary collection of tools. "
         "Registration is opened through a form builder, payment proof is uploaded to a shared drive, "
         "participants are copied into a spreadsheet, and race-day check-in is handled with printed "
         "lists. Announcements are broadcast through social media and messaging groups, while results "
         "are published as a PDF after the event.")
    para(doc,
         "This approach works for small events, but it breaks down as soon as entries grow. The same "
         "runner may appear twice, a payment may not match an entry, a category may be oversold, and "
         "nobody can prove who changed a record. Organisers spend the last week before an event "
         "reconciling data instead of preparing the race, and participants lose confidence when their "
         "entry cannot be found.")

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
         "RunEase addresses each problem with a specific capability rather than a general promise. The "
         "table below links the identified problem to the feature that resolves it and to the evidence "
         "that can be demonstrated in the running system.")
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
        "Technical feasibility: the system uses the Java EE technologies covered in CSC584 and runs on a "
        "standard servlet container. H2 provides a zero-configuration database for demonstration, while "
        "the same schema can be deployed on MySQL.",
        "Operational feasibility: the interface uses familiar web patterns such as search, filters, "
        "modals, confirmation dialogs and dashboards, so organisers do not need specialist training.",
        "Economic feasibility: the application is built entirely with open-source components and can be "
        "hosted on a low-cost servlet container. It removes manual reconciliation work and reduces the "
        "risk of costly registration errors.",
        "Schedule feasibility: the project is delivered as a single Maven WAR with seed data, demo "
        "accounts and a documented run command, which keeps demonstration and evaluation predictable.",
    ])


def objectives(doc):
    page_break(doc)
    h1(doc, "3.0  Objective")

    h2(doc, "3.1  General Objective")
    para(doc,
         "To design and develop a secure, responsive and auditable Java EE web application that manages "
         "the complete lifecycle of running events for participants, organisers and administrators.")

    h2(doc, "3.2  Specific Objectives")
    numbered(doc, [
        "To provide a public, searchable catalogue of approved running events with category, location, "
        "date and price information.",
        "To implement a guided registration and checkout process that validates participant details, "
        "enforces category quotas and produces a unique registration code and bib number.",
        "To give participants self-service control over their entries, including payment of reserved "
        "entries, cancellation and profile updates.",
        "To give organisers a complete workspace for drafting events, managing categories, submitting for "
        "approval, checking participants in, recording results and publishing announcements.",
        "To give administrators governance tools for organiser verification, event review, user account "
        "management, reporting and audit investigation.",
        "To apply enterprise security practices: password hashing, session management, role-based access "
        "control, CSRF protection, security headers and audit logging.",
        "To deliver a consistent, responsive and accessible user interface with breadcrumbs, clear error "
        "messages and confirmation for destructive actions.",
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
     "The landing page introduces the platform, highlights the three role journeys and surfaces approved "
     "events. The public shell uses the same navigation and visual language as the authenticated application."),
    ("02-catalog.jpeg", "Figure 4.2 — Event catalogue with search, filters, sorting and pagination",
     "Visitors can search by keyword and narrow results by event type, state and date. Sort links and page "
     "links preserve the active query string, and each card shows the next important decision: date, location, "
     "distance and entry price."),
    ("04-event-detail.jpeg", "Figure 4.3 — Event detail and race categories",
     "The event page presents the race story, schedule, venue and category table. Registration state is "
     "computed per category, so a closed, full or not-yet-open category is clearly explained instead of "
     "showing a dead button."),
    ("03-participant-dashboard.jpeg", "Figure 4.4 — Participant dashboard",
     "The participant dashboard summarises upcoming races, confirmed entries, spending and recent activity. "
     "It is the runner's home for payments, bib details and cancellations."),
    ("05-checkout.jpeg", "Figure 4.5 — Checkout with dual-layer validation",
     "Checkout collects jersey size, emergency contact, medical notes and payment method. Required fields "
     "are marked, client constraints give immediate feedback, and the server repeats every check before "
     "creating the registration and payment records."),
    ("06-registration-detail.jpeg", "Figure 4.6 — Registration detail, payment and bib card",
     "The registration page is the participant's proof of entry. It shows the registration code, status, "
     "category, bib number, payment method, emergency and medical notes, and offers pay-later or "
     "confirmation-protected cancellation."),
    ("07-organizer-dashboard.jpeg", "Figure 4.7 — Organiser dashboard",
     "The organiser dashboard combines event status, registration trends, revenue and race-day readiness. "
     "Cards link directly to the next operational task, such as reviewing a draft or checking in runners."),
    ("08-organizer-events.jpeg", "Figure 4.8 — Organiser event list",
     "Event management uses a searchable, filterable and sortable table with status pills and contextual "
     "actions. Destructive actions are protected by a confirmation dialog, and edit forms open pre-filled."),
    ("09-organizer-participants.jpeg", "Figure 4.9 — Participant management and check-in",
     "Organisers can filter runners by event, category and status, then check a runner in with one "
     "confirmed action. The action is written to the audit log with the organiser's account and IP address."),
    ("10-organizer-reports.jpeg", "Figure 4.10 — Reports with filters and CSV export",
     "Reports provide live totals for registrations, revenue and attendance. The same filter bar drives the "
     "table and the CSV export, so the downloaded file always matches what the organiser sees."),
    ("11-admin-dashboard.jpeg", "Figure 4.11 — Administrator dashboard",
     "The admin dashboard gives a platform-wide view of users, organisers, events, registrations, revenue "
     "and pending approvals, with direct links into the review queues."),
    ("12-admin-events.jpeg", "Figure 4.12 — Event review and approval",
     "Administrators review submitted events in context and either approve them for the public catalogue or "
     "reject them with a reason. The decision and reason are stored and shown to the organiser."),
    ("13-admin-users.jpeg", "Figure 4.13 — Asynchronous user management",
     "The user page loads and mutates accounts through a JSON API. Create and edit operations use a modal, "
     "field errors return as 422 responses, and status changes are applied without leaving the page."),
    ("14-admin-audit.jpeg", "Figure 4.14 — Audit trail",
     "Every sensitive action is recorded with actor, action, entity, description, IP address and timestamp. "
     "The audit page supports filtering and paging so that an incident can be reconstructed quickly."),
]


def interface(doc):
    page_break(doc)
    h1(doc, "4.0  Proposed User Interface")

    h2(doc, "4.1  Design Direction")
    para(doc,
         "RunEase uses a deliberately restrained visual language: deep navy surfaces, a lime accent for "
         "primary actions and status highlights, generous spacing and a strong typographic hierarchy. "
         "The interface is designed for two very different contexts: a participant browsing on a phone "
         "and an organiser working through a dense operational table on a laptop.")
    bullets(doc, [
        "Palette: navy #0B1220, lime #C9F04C, slate #5C677D and a soft paper background for data-heavy screens.",
        "Typography: Inter for interface text and Space Grotesk for display headings.",
        "Layout: responsive CSS grid and flexbox, with a collapsible sidebar on small screens and a sticky top bar.",
        "Components: cards, status pills, timelines, tabs, filter bars, data tables, modals, toasts and empty states.",
        "Accessibility: semantic landmarks, visible focus styles, labelled form controls, skip link, ARIA live "
        "regions for flash messages and sufficient colour contrast.",
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
         "Every authenticated page shares the same application shell: a role-aware sidebar, a top bar with "
         "search and account menu, a breadcrumb trail and a flash-message region. This consistency is "
         "important because a single user may hold different responsibilities over time and because "
         "deep links from reports must still make sense in context.")

    h2(doc, "4.3  Proposed Screens")
    para(doc,
         "The following screens were captured from the implemented system. Together they cover the public "
         "journey, the participant journey, the organiser workspace and the administrator workspace, "
         "exceeding the minimum of five proposed screens.")

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
         "The layout is tested from 360 px phone widths to desktop widths. Data tables collapse into "
         "horizontally scrollable regions rather than breaking the page, filter bars wrap, the sidebar "
         "becomes an off-canvas drawer, and primary actions remain reachable with one thumb. The same "
         "templates are used across roles, which keeps the responsive behaviour predictable.")


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
         "The database is designed in third normal form around four questions: who is acting, what event "
         "are they running, what entry did they buy, and what happened on race day. Each entity has a "
         "single responsibility, and every relationship is enforced with a foreign key. Enumerated values "
         "are protected by CHECK constraints so that an invalid status cannot enter the database even if "
         "application validation is bypassed.")
    bullets(doc, [
        "Normalisation: repeating data such as category names and event titles is stored once and referenced by key.",
        "Referential integrity: foreign keys protect organiser, category, registration, payment and result links.",
        "Business rules: unique registration code and bib, unique payment and result per registration, one "
        "active entry per participant per event, and positive quotas, prices and finish times.",
        "Soft deletion: events, users, announcements and registrations keep deleted_at markers so that history "
        "and audit references remain valid.",
        "Indexes: role and status, event date, category status, registration date, payment status, result time, "
        "announcement event and audit timestamp.",
        "Transactions: quota checks, registration creation, payment confirmation, approval and result ranking run "
        "inside database transactions.",
    ])

    h2(doc, "5.2  Table Summary")
    data_table(doc,
               ("Table", "Responsibility", "Purpose"),
               TABLE_PURPOSE,
               widths=(3.1, 3.7, 9.5))

    h2(doc, "5.3  Data Dictionary")
    para(doc,
         "The following tables document the physical schema. Types are shown in MySQL/H2 form; the "
         "application schema uses the same definitions for both databases.")
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
         "The ERD below shows the nine tables and their relationships. Solid lines represent the primary "
         "transaction paths; dashed lines represent secondary actor references such as the administrator "
         "who verified an organiser or the organiser who recorded a result.", size=9.5)
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
        "The application never stores plaintext passwords; BCrypt hashes are compared during login.",
        "Registration creation locks the category row before checking the quota, preventing two simultaneous "
        "checkouts from overselling the last slot.",
        "Active-entry checks run in the same transaction as registration creation, backed by application logic "
        "and a filtered query for non-cancelled entries.",
        "Unique constraints on registration_code, bib_number, reference_no and the payment/result registration "
        "links protect against duplicate records.",
        "Audit records are written inside the same transaction as the business change, so an action cannot "
        "succeed without its audit entry.",
        "Database credentials and the JDBC URL are supplied through environment variables, not hard-coded in "
        "production configuration.",
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
         "The flowchart below separates the visitor and authentication layer from the three role-based "
         "workspaces. Solid arrows show the main path; dashed arrows show cross-role handoffs such as an "
         "organiser submitting an event for approval, an administrator approving it, or a confirmed "
         "registration appearing in the organiser's participant list.")
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
         "The flow begins with public discovery, because most runners want to compare events before they "
         "commit to an account. Authentication is deliberately placed at the point of action rather than "
         "at the entrance: a visitor can browse the catalogue and open an event, but checkout redirects "
         "to login and returns to the selected category afterwards.")
    para(doc,
         "After login, the role guard selects the correct workspace. A participant sees the runner "
         "dashboard; an organiser sees event and operational tools; an administrator sees governance "
         "queues. The same account cannot cross those boundaries by editing a URL, because every "
         "controller checks the role and, for record-level actions, the ownership of the record.")
    para(doc,
         "The organiser and administrator flows meet at the approval boundary. An event exists only as a "
         "draft until the organiser adds at least one category and submits it. The administrator can then "
         "approve it, which publishes it to the catalogue, or reject it with a reason that the organiser "
         "can act on. This boundary prevents unverified or incomplete events from accepting money.")
    para(doc,
         "The registration flow is transactional. The system locks the category row, checks the quota and "
         "the participant's active entries, creates the registration and payment records, assigns the "
         "next bib number when payment is confirmed, and writes an audit entry. If any step fails, the "
         "transaction rolls back and the participant receives a recoverable error instead of a partial "
         "registration.")

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
         "A successful login creates a new session identifier, stores only the user identity and role in "
         "the session, and issues a synchroniser token for CSRF protection. Every state-changing request "
         "must present that token. The authentication filter checks the session and role before the "
         "controller runs, while the security headers filter adds CSP, frame, MIME-sniffing, referrer and "
         "permissions policies. Sensitive responses are marked no-store so that personal data does not "
         "remain in a shared browser cache.")


def conclusion(doc):
    page_break(doc)
    h1(doc, "7.0  Conclusion")
    para(doc,
         "RunEase demonstrates how a well-structured Java EE application can solve a real operational "
         "problem without sacrificing security or usability. The proposal has described the gap between "
         "the current manual administration of running events and the needs of modern participants, "
         "organisers and administrators, and it has mapped each problem to a concrete system capability.")
    para(doc,
         "The implemented design uses the MVC pattern and a normalised relational schema to keep business "
         "rules in one place. Dual-layer validation, transactional registration, role-based access control, "
         "CSRF protection and audit logging make the system trustworthy. The interface applies a "
         "consistent design system across public, participant, organiser and administrator journeys, and "
         "the reporting layer turns operational data into decisions instead of spreadsheets.")
    para(doc,
         "The next step is to deploy the WAR to a shared servlet container, connect a production MySQL "
         "instance, replace the simulated payment adapter with a real gateway and add automated JUnit and "
         "browser tests. With those extensions, RunEase can move from an academic prototype to a "
         "production service for race organisers while preserving the same architecture and security model.")
    para(doc,
         "In summary, the project meets its objectives: it provides a complete event lifecycle, protects "
         "each role's data, supports fast and accurate race-day operations, and gives administrators the "
         "evidence they need to govern the platform.")


def appendices(doc):
    page_break(doc)
    h1(doc, "Appendix A — Demonstration Accounts and Setup")
    para(doc,
         "The application ships with generated demonstration data: 20 users, 3 organisers, 10 events, "
         "59 registrations, 13 results and audit history. The database schema and seed run automatically "
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
        "Default: embedded H2 in MySQL compatibility mode at ./data/runease; schema and seed are applied automatically.",
        "MySQL: create the schema from database/schema_mysql.sql, load database/seed_mysql.sql, then set "
        "RUNEASE_DB_URL, RUNEASE_DB_USER and RUNEASE_DB_PASSWORD before starting the application.",
        "The WAR runs on any Jakarta EE 10 servlet container; the embedded Tomcat runner is for demonstration only.",
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
