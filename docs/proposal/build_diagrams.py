"""Generate the RunEase ERD and application-flow diagrams for the proposal."""

from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import FancyBboxPatch, Polygon

OUT = Path(__file__).resolve().parent / "images"
OUT.mkdir(parents=True, exist_ok=True)

NAVY = "#0b1220"
INK = "#1c2433"
SLATE = "#5c677d"
LIME = "#c9f04c"
MINT = "#e9f8c9"
PAPER = "#f7f9fc"
WHITE = "#ffffff"
BLUE = "#2f6fed"
AMBER = "#d98b0b"
RED = "#c94a4a"

plt.rcParams["font.family"] = "DejaVu Sans"


# ---------------------------------------------------------------------------
# ERD
# ---------------------------------------------------------------------------
TABLES = {
    "users": {
        "x": 6.0, "top": 12.9, "w": 4.0,
        "fields": [
            ("user_id", "BIGINT", "PK"),
            ("full_name", "VARCHAR(120)", ""),
            ("email", "VARCHAR(160)", "UQ"),
            ("password_hash", "VARCHAR(100)", ""),
            ("role", "VARCHAR(20)", ""),
            ("status", "VARCHAR(20)", ""),
        ],
    },
    "organizers": {
        "x": 11.0, "top": 12.9, "w": 4.8,
        "fields": [
            ("organizer_id", "BIGINT", "PK"),
            ("user_id", "BIGINT", "FK"),
            ("organization_name", "VARCHAR(150)", ""),
            ("verification_status", "VARCHAR(20)", ""),
            ("verified_by", "BIGINT", "FK"),
        ],
    },
    "events": {
        "x": 11.0, "top": 10.2, "w": 4.8,
        "fields": [
            ("event_id", "BIGINT", "PK"),
            ("organizer_id", "BIGINT", "FK"),
            ("title", "VARCHAR(150)", ""),
            ("event_date", "DATE", ""),
            ("status", "VARCHAR(20)", ""),
        ],
    },
    "event_categories": {
        "x": 11.0, "top": 7.5, "w": 4.8,
        "fields": [
            ("category_id", "BIGINT", "PK"),
            ("event_id", "BIGINT", "FK"),
            ("name", "VARCHAR(60)", ""),
            ("distance_km", "DECIMAL(5,1)", ""),
            ("price", "DECIMAL(10,2)", ""),
            ("quota", "INT", ""),
        ],
    },
    "registrations": {
        "x": 6.0, "top": 10.2, "w": 4.2,
        "fields": [
            ("registration_id", "BIGINT", "PK"),
            ("registration_code", "VARCHAR(20)", "UQ"),
            ("user_id", "BIGINT", "FK"),
            ("category_id", "BIGINT", "FK"),
            ("bib_number", "VARCHAR(15)", ""),
            ("status", "VARCHAR(20)", ""),
            ("total_amount", "DECIMAL(10,2)", ""),
        ],
    },
    "payments": {
        "x": 6.0, "top": 7.0, "w": 4.2,
        "fields": [
            ("payment_id", "BIGINT", "PK"),
            ("registration_id", "BIGINT", "FK"),
            ("reference_no", "VARCHAR(40)", "UQ"),
            ("method", "VARCHAR(20)", ""),
            ("amount", "DECIMAL(10,2)", ""),
            ("status", "VARCHAR(20)", ""),
        ],
    },
    "results": {
        "x": 6.0, "top": 3.8, "w": 4.2,
        "fields": [
            ("result_id", "BIGINT", "PK"),
            ("registration_id", "BIGINT", "FK"),
            ("finish_time_seconds", "INT", ""),
            ("overall_position", "INT", ""),
            ("category_position", "INT", ""),
        ],
    },
    "announcements": {
        "x": 11.0, "top": 3.8, "w": 4.8,
        "fields": [
            ("announcement_id", "BIGINT", "PK"),
            ("event_id", "BIGINT", "FK"),
            ("created_by", "BIGINT", "FK"),
            ("title", "VARCHAR(150)", ""),
            ("audience", "VARCHAR(20)", ""),
        ],
    },
    "audit_logs": {
        "x": 0.2, "top": 8.6, "w": 4.8,
        "fields": [
            ("log_id", "BIGINT", "PK"),
            ("user_id", "BIGINT", "FK"),
            ("action", "VARCHAR(30)", ""),
            ("entity_type", "VARCHAR(40)", ""),
            ("entity_id", "VARCHAR(40)", ""),
            ("ip_address", "VARCHAR(45)", ""),
        ],
    },
}


def draw_erd():
    fig, ax = plt.subplots(figsize=(16, 11.6), dpi=200)
    ax.set_xlim(0, 16.2)
    ax.set_ylim(0, 13.4)
    ax.axis("off")
    fig.patch.set_facecolor(WHITE)

    ax.text(0.2, 13.12, "RunEase — Entity Relationship Diagram",
            fontsize=20, fontweight="bold", color=NAVY)
    ax.text(0.2, 12.82,
            "Nine tables in third normal form. Solid lines are primary relationships; dashed lines are secondary actor references.",
            fontsize=9.5, color=SLATE)

    boxes = {}
    for name, spec in TABLES.items():
        x, top, w = spec["x"], spec["top"], spec["w"]
        rows = spec["fields"]
        header_h = 0.38
        row_h = 0.205
        h = header_h + row_h * len(rows) + 0.06
        bottom = top - h
        boxes[name] = {"x": x, "y": bottom, "w": w, "h": h,
                       "top": top, "right": x + w, "bottom": bottom}

        shadow = FancyBboxPatch((x + 0.045, bottom - 0.045), w, h,
                                boxstyle="round,pad=0,rounding_size=0.08",
                                linewidth=0, facecolor="#d7dde8", zorder=1)
        ax.add_patch(shadow)
        card = FancyBboxPatch((x, bottom), w, h,
                              boxstyle="round,pad=0,rounding_size=0.08",
                              linewidth=1.0, edgecolor="#c4cddb",
                              facecolor=WHITE, zorder=2)
        ax.add_patch(card)
        head = FancyBboxPatch((x, top - header_h), w, header_h,
                              boxstyle="round,pad=0,rounding_size=0.08",
                              linewidth=0, facecolor=NAVY, zorder=3)
        ax.add_patch(head)
        ax.text(x + 0.14, top - header_h / 2 + 0.01, name,
                fontsize=10.5, fontweight="bold", color=LIME, va="center", zorder=4)

        for i, (field, ftype, key) in enumerate(rows):
            cy = top - header_h - row_h * (i + 0.5) - 0.015
            if i % 2 == 0:
                stripe = plt.Rectangle((x + 0.02, cy - row_h / 2), w - 0.04, row_h,
                                       linewidth=0, facecolor=PAPER, zorder=2.5)
                ax.add_patch(stripe)
            key_color = AMBER if key == "PK" else (BLUE if key in ("FK", "UQ") else None)
            if key:
                ax.text(x + 0.12, cy, key, fontsize=6.4, fontweight="bold",
                        color=key_color, va="center", zorder=5)
            ax.text(x + 0.46, cy, field, fontsize=7.4, color=INK,
                    va="center", family="DejaVu Sans Mono", zorder=5)
            ax.text(x + w - 0.1, cy, ftype, fontsize=6.4, color=SLATE,
                    va="center", ha="right", family="DejaVu Sans Mono", zorder=5)

    def arrow(points, color=INK, dashed=False, label=None, lx=None, ly=None,
              lcolor=None, lsize=7.0, rad=0.0):
        style = (0, (3.2, 2.4)) if dashed else "solid"
        for i in range(len(points) - 1):
            x1, y1 = points[i]
            x2, y2 = points[i + 1]
            last = i == len(points) - 2
            if last:
                ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                            arrowprops=dict(arrowstyle="-|>", color=color, lw=1.15,
                                            linestyle=style, shrinkA=0, shrinkB=1.5,
                                            mutation_scale=11,
                                            connectionstyle=f"arc3,rad={rad}"),
                            zorder=6)
            else:
                ax.plot([x1, x2], [y1, y2], color=color, lw=1.15,
                        linestyle=style, zorder=6, solid_capstyle="round")
        if label:
            ax.text(lx, ly, label, fontsize=lsize, color=lcolor or color,
                    ha="center", va="center", zorder=7,
                    bbox=dict(boxstyle="round,pad=0.15", facecolor=WHITE,
                              edgecolor="none", alpha=0.92))

    u, o, e, c = boxes["users"], boxes["organizers"], boxes["events"], boxes["event_categories"]
    r, p, rs = boxes["registrations"], boxes["payments"], boxes["results"]
    a, au = boxes["announcements"], boxes["audit_logs"]

    arrow([(u["right"], 12.25), (o["x"], 12.25)], label="1 : N", lx=10.5, ly=12.42)
    arrow([(o["x"] + o["w"] / 2, o["bottom"]), (e["x"] + e["w"] / 2, e["top"])],
          label="1 : N", lx=13.55, ly=11.78)
    arrow([(e["x"] + e["w"] / 2, e["bottom"]), (c["x"] + c["w"] / 2, c["top"])],
          label="1 : N", lx=13.55, ly=8.98)

    arrow([(u["x"] + u["w"] / 2, u["bottom"]), (r["x"] + r["w"] / 2, r["top"])],
          label="1 : N", lx=8.33, ly=10.72)
    arrow([(r["x"] + r["w"] / 2, r["bottom"]), (p["x"] + p["w"] / 2, p["top"])],
          label="1 : 1", lx=8.33, ly=7.67)
    arrow([(r["x"], 9.2), (5.55, 9.2), (5.55, 3.05), (rs["x"], 3.05)],
          label="1 : 0..1", lx=5.55, ly=6.1, lsize=6.8)
    arrow([(r["right"], 8.85), (c["x"], 7.15)],
          label="N : 1", lx=10.62, ly=8.05)
    arrow([(e["right"], 9.5), (16.05, 9.5), (16.05, 3.05), (a["right"], 3.05)],
          label="1 : N", lx=16.05, ly=6.2)
    arrow([(u["x"], 12.2), (au["right"], au["top"])], dashed=True,
          label="actor", lx=4.98, ly=10.5, lsize=6.6)
    arrow([(u["x"], 11.6), (0.0, 11.6), (0.0, 2.6), (rs["x"], 2.6)], dashed=True,
          label="recorded_by", lx=0.75, ly=7.4, lsize=6.6)
    arrow([(o["x"], 12.6), (u["right"], 12.6)], dashed=True)

    ax.text(0.2, 0.55,
            "PK  primary key     FK  foreign key     UQ  unique constraint     1 : N  one-to-many     "
            "1 : 1  one-to-one     N : 1  many-to-one",
            fontsize=7.6, color=SLATE)

    fig.savefig(OUT / "erd.png", dpi=200, bbox_inches="tight",
                facecolor=WHITE, pad_inches=0.15)
    plt.close(fig)


# ---------------------------------------------------------------------------
# Application flow (swimlane flowchart)
# ---------------------------------------------------------------------------
def draw_flow():
    fig, ax = plt.subplots(figsize=(24, 15.2), dpi=170)
    ax.set_xlim(0, 24)
    ax.set_ylim(0, 15.2)
    ax.axis("off")
    fig.patch.set_facecolor(WHITE)

    ax.text(0.35, 14.92, "RunEase — Flow of Application",
            fontsize=22, fontweight="bold", color=NAVY)
    ax.text(0.35, 14.6,
            "Public browsing, authentication, role-based workspaces, approval workflow and participant lifecycle.",
            fontsize=10, color=SLATE)

    lanes = [
        ("Visitor / Auth", 13.35, 15.0, "#eef2f7", SLATE),
        ("Participant", 9.35, 13.35, "#eaf1ff", BLUE),
        ("Organizer", 5.35, 9.35, "#f0f8db", "#5f7d12"),
        ("Administrator", 1.15, 5.35, "#fdf3e2", AMBER),
    ]
    for name, bottom, top, fill, accent in lanes:
        ax.add_patch(plt.Rectangle((2.55, bottom), 21.1, top - bottom,
                                   facecolor=fill, edgecolor="none", zorder=0))
        ax.plot([2.55, 23.65], [top, top], color=WHITE, lw=3, zorder=1)
        ax.plot([2.55, 2.55], [bottom, top], color=accent, lw=3, zorder=1)
        ax.text(0.35, (bottom + top) / 2, name, fontsize=12, fontweight="bold",
                color=accent, rotation=90, va="center", ha="center")

    nodes = {}

    def box(key, x, y, text, lane_color=NAVY, w=2.75, h=0.72, fill=WHITE,
            fontsize=8.4, bold=False):
        p = FancyBboxPatch((x - w / 2, y - h / 2), w, h,
                           boxstyle="round,pad=0.02,rounding_size=0.12",
                           linewidth=1.15, edgecolor=lane_color, facecolor=fill, zorder=3)
        ax.add_patch(p)
        ax.text(x, y, text, ha="center", va="center", fontsize=fontsize,
                color=INK, fontweight="bold" if bold else "normal", zorder=4,
                linespacing=1.25)
        nodes[key] = {"x": x, "y": y, "w": w, "h": h,
                      "left": x - w / 2, "right": x + w / 2,
                      "top": y + h / 2, "bottom": y - h / 2}

    def terminal(key, x, y, text, color=NAVY):
        p = FancyBboxPatch((x - 1.1, y - 0.36), 2.2, 0.72,
                           boxstyle="round,pad=0.02,rounding_size=0.36",
                           linewidth=1.2, edgecolor=color, facecolor=NAVY, zorder=3)
        ax.add_patch(p)
        ax.text(x, y, text, ha="center", va="center", fontsize=8.6,
                color=LIME, fontweight="bold", zorder=4)
        nodes[key] = {"x": x, "y": y, "w": 2.2, "h": 0.72,
                      "left": x - 1.1, "right": x + 1.1,
                      "top": y + 0.36, "bottom": y - 0.36}

    def diamond(key, x, y, text, color=AMBER, w=2.9, h=1.15, fontsize=7.8):
        pts = [(x, y + h / 2), (x + w / 2, y), (x, y - h / 2), (x - w / 2, y)]
        ax.add_patch(Polygon(pts, closed=True, facecolor="#fff8ea",
                             edgecolor=color, linewidth=1.15, zorder=3))
        ax.text(x, y, text, ha="center", va="center", fontsize=fontsize,
                color=INK, zorder=4, linespacing=1.15)
        nodes[key] = {"x": x, "y": y, "w": w, "h": h,
                      "left": x - w / 2, "right": x + w / 2,
                      "top": y + h / 2, "bottom": y - h / 2}

    def arrow(a, b, points=None, color=SLATE, dashed=False, label=None,
              lx=None, ly=None, lsize=7.2, ha="center"):
        style = (0, (4, 3)) if dashed else "solid"
        if points is None:
            points = [(nodes[a]["x"], nodes[a]["y"]), (nodes[b]["x"], nodes[b]["y"])]
        for i in range(len(points) - 1):
            x1, y1 = points[i]
            x2, y2 = points[i + 1]
            if i == len(points) - 2:
                ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                            arrowprops=dict(arrowstyle="-|>", color=color, lw=1.25,
                                            linestyle=style, mutation_scale=12,
                                            shrinkA=0, shrinkB=1.0), zorder=5)
            else:
                ax.plot([x1, x2], [y1, y2], color=color, lw=1.25,
                        linestyle=style, zorder=5)
        if label:
            ax.text(lx, ly, label, fontsize=lsize, color=color,
                    ha=ha, va="center", zorder=6,
                    bbox=dict(boxstyle="round,pad=0.18", facecolor=WHITE,
                              edgecolor="none", alpha=0.95))

    # Visitor / auth lane
    terminal("start", 4.4, 14.15, "Start")
    box("catalogue", 7.85, 14.15, "Public catalogue\nfilter · search · sort")
    box("auth", 11.35, 14.15, "Sign in or\ncreate account")
    diamond("valid", 14.9, 14.15, "Credentials\nvalid?")
    box("session", 18.6, 14.15, "Session created\n+ role guard", lane_color=NAVY, bold=True)

    # Participant lane
    box("p_dash", 4.4, 12.3, "Participant\ndashboard", BLUE)
    box("p_browse", 7.85, 12.3, "Browse approved\nevents", BLUE)
    box("p_detail", 11.35, 12.3, "Event detail +\nrace categories", BLUE)
    box("p_checkout", 14.9, 12.3, "Checkout form\n(client + server validation)", BLUE)
    box("p_pay", 18.6, 12.3, "Pay now or\nreserve entry", BLUE)
    box("p_confirmed", 18.6, 10.35, "Entry confirmed\nbib issued", BLUE, fill=MINT)
    box("p_manage", 14.9, 10.35, "Manage entry\npay · cancel", BLUE)
    box("p_error", 7.85, 10.35, "Cancelled entry\n+ refund status", BLUE, fill="#fff1f1")
    terminal("p_end", 4.4, 10.35, "End")

    # Organizer lane
    box("o_dash", 4.4, 8.3, "Organizer\ndashboard", "#5f7d12")
    box("o_create", 7.85, 8.3, "Create event\n(DRAFT)", "#5f7d12")
    box("o_cat", 11.35, 8.3, "Add race\ncategories", "#5f7d12")
    box("o_submit", 14.9, 8.3, "Submit for\napproval (PENDING)", "#5f7d12")
    box("o_manage", 18.6, 6.25, "Manage\nparticipants", "#5f7d12")
    box("o_checkin", 14.9, 6.25, "Race-day\ncheck-in", "#5f7d12")
    box("o_results", 11.35, 6.25, "Record\nfinish times", "#5f7d12")
    box("o_reports", 7.85, 6.25, "Reports, export\n+ announcements", "#5f7d12", fill=MINT)
    terminal("o_end", 4.4, 6.25, "End")

    # Administrator lane
    box("a_dash", 4.4, 4.3, "Admin\ndashboard", AMBER)
    box("a_orgs", 7.85, 4.3, "Verify\norganizers", AMBER)
    box("a_queue", 11.35, 4.3, "Review event\nqueue", AMBER)
    diamond("a_decision", 14.9, 4.3, "Approve or\nreject?", AMBER)
    box("a_users", 11.35, 2.25, "Manage user\naccounts", AMBER)
    box("a_audit", 7.85, 2.25, "Audit trail\n+ reports", AMBER, fill=MINT)
    terminal("a_end", 4.4, 2.25, "End")

    # Main horizontal flows
    arrow("start", "catalogue", [(nodes["start"]["right"], 14.15), (nodes["catalogue"]["left"], 14.15)])
    arrow("catalogue", "auth", [(nodes["catalogue"]["right"], 14.15), (nodes["auth"]["left"], 14.15)])
    arrow("auth", "valid", [(nodes["auth"]["right"], 14.15), (nodes["valid"]["left"], 14.15)])
    arrow("valid", "session", [(nodes["valid"]["right"], 14.15), (nodes["session"]["left"], 14.15)],
          label="yes", lx=16.95, ly=14.32, lsize=7)

    arrow("p_dash", "p_browse", [(nodes["p_dash"]["right"], 12.3), (nodes["p_browse"]["left"], 12.3)])
    arrow("p_browse", "p_detail", [(nodes["p_browse"]["right"], 12.3), (nodes["p_detail"]["left"], 12.3)])
    arrow("p_detail", "p_checkout", [(nodes["p_detail"]["right"], 12.3), (nodes["p_checkout"]["left"], 12.3)])
    arrow("p_checkout", "p_pay", [(nodes["p_checkout"]["right"], 12.3), (nodes["p_pay"]["left"], 12.3)])
    arrow("p_pay", "p_confirmed", [(18.6, nodes["p_pay"]["bottom"]), (18.6, nodes["p_confirmed"]["top"])])
    arrow("p_confirmed", "p_manage", [(nodes["p_confirmed"]["left"], 10.35), (nodes["p_manage"]["right"], 10.35)])
    arrow("p_manage", "p_error", [(nodes["p_manage"]["left"], 10.35), (nodes["p_error"]["right"], 10.35)],
          label="cancel", lx=11.42, ly=10.55, lsize=7)
    arrow("p_error", "p_end", [(nodes["p_error"]["left"], 10.35), (nodes["p_end"]["right"], 10.35)])

    arrow("o_dash", "o_create", [(nodes["o_dash"]["right"], 8.3), (nodes["o_create"]["left"], 8.3)])
    arrow("o_create", "o_cat", [(nodes["o_create"]["right"], 8.3), (nodes["o_cat"]["left"], 8.3)])
    arrow("o_cat", "o_submit", [(nodes["o_cat"]["right"], 8.3), (nodes["o_submit"]["left"], 8.3)])
    arrow("o_submit", "o_manage", [(14.9, nodes["o_submit"]["bottom"]), (14.9, 7.35), (18.6, 7.35), (18.6, nodes["o_manage"]["top"])])
    arrow("o_manage", "o_checkin", [(nodes["o_manage"]["left"], 6.25), (nodes["o_checkin"]["right"], 6.25)])
    arrow("o_checkin", "o_results", [(nodes["o_checkin"]["left"], 6.25), (nodes["o_results"]["right"], 6.25)])
    arrow("o_results", "o_reports", [(nodes["o_results"]["left"], 6.25), (nodes["o_reports"]["right"], 6.25)])
    arrow("o_reports", "o_end", [(nodes["o_reports"]["left"], 6.25), (nodes["o_end"]["right"], 6.25)])

    arrow("a_dash", "a_orgs", [(nodes["a_dash"]["right"], 4.3), (nodes["a_orgs"]["left"], 4.3)])
    arrow("a_orgs", "a_queue", [(nodes["a_orgs"]["right"], 4.3), (nodes["a_queue"]["left"], 4.3)])
    arrow("a_queue", "a_decision", [(nodes["a_queue"]["right"], 4.3), (nodes["a_decision"]["left"], 4.3)])
    arrow("a_decision", "a_users", [(14.9, nodes["a_decision"]["bottom"]), (14.9, 3.25), (11.35, 3.25), (11.35, nodes["a_users"]["top"])])
    arrow("a_users", "a_audit", [(nodes["a_users"]["left"], 2.25), (nodes["a_audit"]["right"], 2.25)])
    arrow("a_audit", "a_end", [(nodes["a_audit"]["left"], 2.25), (nodes["a_end"]["right"], 2.25)])

    # Role bus from session
    bus_x = 2.95
    ax.plot([nodes["session"]["x"], nodes["session"]["x"]],
            [nodes["session"]["top"], 15.05], color=NAVY, lw=1.25, zorder=5)
    ax.plot([nodes["session"]["x"], bus_x], [15.05, 15.05], color=NAVY, lw=1.25, zorder=5)
    ax.plot([bus_x, bus_x], [15.05, nodes["a_dash"]["y"]], color=NAVY, lw=1.25, zorder=5)
    for key, color in (("p_dash", BLUE), ("o_dash", "#5f7d12"), ("a_dash", AMBER)):
        ax.annotate("", xy=(nodes[key]["left"], nodes[key]["y"]),
                    xytext=(bus_x, nodes[key]["y"]),
                    arrowprops=dict(arrowstyle="-|>", color=color, lw=1.25,
                                    mutation_scale=11, shrinkA=0, shrinkB=1.0), zorder=5)
    ax.text(3.35, 13.55, "role-based redirect", fontsize=7.2, color=NAVY,
            rotation=90, va="center", ha="left")

    # Cross-role handoffs
    arrow("o_submit", "a_queue",
          [(14.9, nodes["o_submit"]["bottom"]), (14.9, 7.35), (9.85, 7.35),
           (9.85, 4.75), (nodes["a_queue"]["left"], 4.75)],
          color=AMBER, dashed=True,
          label="1  approval request", lx=12.35, ly=7.62, lsize=7.2)
    arrow("a_decision", "o_manage",
          [(nodes["a_decision"]["right"], 4.3), (20.35, 4.3), (20.35, 5.55),
           (18.6, 5.55), (18.6, nodes["o_manage"]["bottom"])],
          color="#5f7d12", dashed=True,
          label="2  approved & published", lx=20.35, ly=4.98, lsize=7.2)
    arrow("a_decision", "o_create",
          [(nodes["a_decision"]["bottom"], 3.85), (15.9, 3.85), (15.9, 1.75),
           (6.35, 1.75), (6.35, 7.4), (7.55, 7.4), (7.55, nodes["o_create"]["bottom"])],
          color=RED, dashed=True,
          label="3  rejected with reason", lx=10.6, ly=2.02, lsize=7.2)
    arrow("p_confirmed", "o_manage",
          [(18.6, nodes["p_confirmed"]["bottom"]), (18.6, nodes["o_manage"]["top"])],
          color=BLUE, dashed=True,
          label="4  new registration", lx=18.6, ly=9.02, lsize=7.2)

    # Visitor invalid login loop
    arrow("valid", "auth",
          [(14.9, nodes["valid"]["top"]), (14.9, 14.95), (11.35, 14.95), (11.35, nodes["auth"]["top"])],
          color=RED, dashed=True, label="no · error message", lx=13.1, ly=14.95, lsize=7)

    # Legend
    ax.plot([0.35, 1.05], [0.72, 0.72], color=SLATE, lw=1.4)
    ax.text(1.2, 0.72, "main flow", fontsize=8, color=SLATE, va="center")
    ax.plot([0.35, 1.05], [0.4, 0.4], color=AMBER, lw=1.4, linestyle=(0, (4, 3)))
    ax.text(1.2, 0.4, "cross-role handoff", fontsize=8, color=SLATE, va="center")
    ax.text(6.5, 0.55,
            "Validation happens twice: browser constraints and server-side checks. "
            "Authentication, role checks and CSRF protect every state-changing action.",
            fontsize=8, color=SLATE, va="center")

    fig.savefig(OUT / "flowchart.png", dpi=170, bbox_inches="tight",
                facecolor=WHITE, pad_inches=0.12)
    plt.close(fig)


if __name__ == "__main__":
    draw_erd()
    draw_flow()
    print("Wrote", OUT / "erd.png")
    print("Wrote", OUT / "flowchart.png")
