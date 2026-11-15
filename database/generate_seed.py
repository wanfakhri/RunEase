"""Generates database/seed.sql (and the copy packaged in the WAR).

The data is deterministic so screenshots and reports always match.
Run:  python database/generate_seed.py
"""
import random
from datetime import date, datetime, timedelta
from pathlib import Path

random.seed(584)

PWD = "$2a$10$MwENM9MtGiQcj.aAnfvOEeqE17pQsQqrEitR3pcJWcB2DZCr4o7Jm"  # Password123!


def q(value):
    if value is None:
        return "NULL"
    if isinstance(value, (int, float)):
        return str(value)
    return "'" + str(value).replace("'", "''") + "'"


users = [
    ("Nurul Aina binti Rahman", "admin@runease.my", "0123456789", "ADMIN", "ACTIVE", "FEMALE", "1991-04-12", "Shah Alam"),
    ("Ahmad Zulfadhli bin Kamarudin", "ahmad@paceevents.my", "0198765432", "ORGANIZER", "ACTIVE", "MALE", "1988-08-30", "Petaling Jaya"),
    ("Siti Hajar binti Osman", "siti@trailworks.my", "0134567890", "ORGANIZER", "ACTIVE", "FEMALE", "1990-02-14", "Kota Kinabalu"),
    ("Muhammad Faiz bin Ismail", "faiz@runlab.my", "0176543210", "ORGANIZER", "ACTIVE", "MALE", "1995-06-21", "Kuala Lumpur"),
    ("Alia Nur binti Hassan", "alia@student.uitm.edu.my", "01123456789", "PARTICIPANT", "ACTIVE", "FEMALE", "2004-01-19", "Shah Alam"),
    ("Daniel Lim Wei Sheng", "daniel@runease.my", "0129876543", "PARTICIPANT", "ACTIVE", "MALE", "1996-11-02", "Subang Jaya"),
    ("Priya Devi a/p Ramesh", "priya@runease.my", "0163456789", "PARTICIPANT", "ACTIVE", "FEMALE", "1993-07-08", "Klang"),
    ("Abdul Hakim bin Zulkifli", "hakim@runease.my", "0187654321", "PARTICIPANT", "ACTIVE", "MALE", "1985-03-27", "Kuala Lumpur"),
    ("Tan Mei Ling", "mei@runease.my", "0123459876", "PARTICIPANT", "ACTIVE", "FEMALE", "1999-09-15", "George Town"),
    ("Arif Danial bin Roslan", "arif@runease.my", "01198765432", "PARTICIPANT", "ACTIVE", "MALE", "2002-12-05", "Ipoh"),
    ("Nabila binti Amir", "nabila@runease.my", "0139876543", "PARTICIPANT", "ACTIVE", "FEMALE", "1997-05-23", "Kajang"),
    ("Kavin Raj a/l Subramaniam", "kavin@runease.my", "0172345678", "PARTICIPANT", "ACTIVE", "MALE", "1994-10-11", "Petaling Jaya"),
    ("Farah Adibah binti Yusof", "farah@runease.my", "0193456787", "PARTICIPANT", "ACTIVE", "FEMALE", "1998-02-18", "Putrajaya"),
    ("Muhammad Zikri bin Azman", "zikri@runease.my", "0145678901", "PARTICIPANT", "INACTIVE", "MALE", "2000-08-09", "Seremban"),
    ("Lim Hui Xin", "hui@runease.my", "0167890123", "PARTICIPANT", "ACTIVE", "FEMALE", "1995-04-30", "Johor Bahru"),
    ("Syafiq Aiman bin Nasir", "syafiq@runease.my", "0189012345", "PARTICIPANT", "ACTIVE", "MALE", "1992-06-14", "Melaka"),
    ("Aisyah binti Kamal", "aisyah@runease.my", "01123456780", "PARTICIPANT", "ACTIVE", "FEMALE", "2001-03-03", "Kuantan"),
    ("Rajesh Kumar a/l Muthu", "raj@runease.my", "0123456712", "PARTICIPANT", "SUSPENDED", "MALE", "1990-12-25", "Shah Alam"),
    ("Sofia Adriana binti Rahim", "sofia@runease.my", "0134567823", "PARTICIPANT", "ACTIVE", "FEMALE", "1999-07-07", "Kuching"),
    ("Haziq bin Firdaus", "haziq@runease.my", "0176543298", "PARTICIPANT", "ACTIVE", "MALE", "2003-09-28", "Alor Setar"),
]

organizers = [
    (2, "Pace Events Sdn Bhd", "SSM 202301234567", "Ahmad Zulfadhli bin Kamarudin", "https://paceevents.my", "VERIFIED", "2026-09-20 10:15:00", 1),
    (3, "TrailWorks Asia Sdn Bhd", "SSM 202204567891", "Siti Hajar binti Osman", "https://trailworks.my", "VERIFIED", "2026-09-22 14:40:00", 1),
    (4, "RunLab Malaysia", "SSM 202401234999", "Muhammad Faiz bin Ismail", "https://runlab.my", "PENDING", None, None),
]

# (title, slug, organizer_id, type, description, venue, city, state, event_date,
#  flag_off, reg_open, reg_close, status, rejection, [(name, km, price, quota, start)])
events = [
    ("Penang Bridge International Marathon 2027", "penang-bridge-marathon-2027", 1, "ROAD",
     "Run across the iconic 13.5 km Penang Bridge at sunrise. A certified AIMS course with full road closure, hydration stations every 2.5 km and a finish festival at Queensbay Mall.",
     "Penang Bridge, Gelugor", "George Town", "Pulau Pinang", "2027-03-14", "04:00:00",
     "2026-10-01", "2027-02-15", "APPROVED", None,
     [("42KM Full Marathon", 42.0, 120.00, 3000, "04:00:00"), ("21KM Half Marathon", 21.0, 95.00, 5000, "05:00:00"),
      ("10KM Open", 10.0, 65.00, 8000, "06:00:00"), ("5KM Fun Run", 5.0, 45.00, 5000, "07:00:00")]),
    ("KL City Night Run 2027", "kl-city-night-run-2027", 1, "ROAD",
     "Experience Kuala Lumpur after dark. A flat city-centre route passing Dataran Merdeka, KLCC and Bukit Bintang with live music at every kilometre.",
     "Dataran Merdeka", "Kuala Lumpur", "W.P. Kuala Lumpur", "2027-01-23", "19:30:00",
     "2026-11-01", "2027-01-10", "APPROVED", None,
     [("10KM Night Dash", 10.0, 75.00, 4000, "19:30:00"), ("5KM Glow Run", 5.0, 55.00, 3000, "20:00:00")]),
    ("UiTM Neon Fun Run 2026", "uitm-neon-fun-run-2026", 2, "FUN_RUN",
     "The biggest student run of the semester. Glow sticks, neon paint, campus DJ and a 5KM route around UiTM Shah Alam. Open to students, staff and the public.",
     "UiTM Shah Alam", "Shah Alam", "Selangor", "2026-12-12", "18:00:00",
     "2026-10-15", "2026-12-05", "APPROVED", None,
     [("5KM Neon Run", 5.0, 45.00, 2500, "18:30:00"), ("3KM Campus Fun Run", 3.0, 35.00, 2000, "19:00:00")]),
    ("Putrajaya Half Marathon 2027", "putrajaya-half-marathon-2027", 1, "ROAD",
     "A scenic lakeside half marathon through the federal administrative capital. Ideal for runners chasing a personal best on a flat, fast course.",
     "Dataran Putrajaya, Presint 3", "Putrajaya", "W.P. Putrajaya", "2027-05-09", "05:00:00",
     "2026-11-01", "2027-04-15", "APPROVED", None,
     [("21KM Half Marathon", 21.0, 95.00, 3500, "05:00:00"), ("10KM Open", 10.0, 70.00, 3000, "06:00:00"),
      ("5KM Community Run", 5.0, 50.00, 2500, "06:30:00")]),
    ("Sabah Highland Trail Challenge 2027", "sabah-highland-trail-2027", 2, "TRAIL",
     "Technical trail racing through the cool highlands of Kundasang with views of Mount Kinabalu. Mandatory kit list applies for the 30KM category.",
     "Kundasang Sports Complex", "Kundasang", "Sabah", "2027-02-06", "05:30:00",
     "2026-11-01", "2027-01-20", "PENDING", None,
     [("30KM Mountain Trail", 30.0, 130.00, 500, "05:30:00"), ("15KM Highland Trail", 15.0, 85.00, 800, "06:30:00")]),
    ("Borneo Ultra 2027", "borneo-ultra-2027", 2, "TRAIL",
     "A 50KM ultra through the rainforest of Kubah National Park. Draft event awaiting final permits.",
     "Kubah National Park", "Kuching", "Sarawak", "2027-04-18", "04:30:00",
     "2026-12-15", "2027-03-20", "DRAFT", None,
     [("50KM Ultra", 50.0, 180.00, 300, "04:30:00")]),
    ("Melaka Heritage Run 2026", "melaka-heritage-run-2026", 1, "ROAD",
     "A heritage trail through the UNESCO-listed streets of Bandar Hilir, finishing at A'Famosa. Completed event with published results.",
     "A'Famosa, Bandar Hilir", "Melaka", "Melaka", "2026-10-04", "06:00:00",
     "2026-07-01", "2026-09-20", "COMPLETED", None,
     [("10KM Heritage Run", 10.0, 60.00, 1500, "06:00:00"), ("5KM Family Run", 5.0, 40.00, 1500, "06:30:00")]),
    ("Langkawi Coastal Run 2027", "langkawi-coastal-run-2027", 2, "ROAD",
     "Island coastal route starting at Pantai Cenang. Proposal rejected pending district council permit and traffic management plan.",
     "Pantai Cenang", "Langkawi", "Kedah", "2027-03-28", "06:00:00",
     "2026-11-15", "2027-02-28", "REJECTED", "Venue permit and traffic management plan were not attached. Please resubmit with the district council approval letter.",
     [("21KM Coastal Run", 21.0, 90.00, 1200, "06:00:00"), ("10KM Beach Run", 10.0, 65.00, 1500, "06:30:00")]),
    ("Merdeka Virtual 65 Challenge", "merdeka-virtual-65-2026", 1, "VIRTUAL",
     "Run 6.5 km anywhere in Malaysia between 1 and 16 September 2026 to celebrate Merdeka. Finisher medals posted nationwide.",
     "Anywhere in Malaysia", "Nationwide", "Malaysia", "2026-09-16", "00:00:00",
     "2026-08-01", "2026-09-16", "COMPLETED", None,
     [("6.5KM Virtual Run", 6.5, 30.00, 5000, "00:00:00")]),
    ("RunEase Training Series - 5K Time Trial", "runease-training-series-5k", 2, "FUN_RUN",
     "Monthly community time trial at Bukit Jalil. Chip-timed 5KM with pacing groups from 20 to 40 minutes.",
     "Bukit Jalil Recreational Park", "Kuala Lumpur", "W.P. Kuala Lumpur", "2026-12-20", "07:00:00",
     "2026-11-01", "2026-12-15", "APPROVED", None,
     [("5KM Time Trial", 5.0, 25.00, 200, "07:00:00")]),
]

category_rows = []
for event_index, ev in enumerate(events, start=1):
    for name, km, price, quota, start in ev[14]:
        category_rows.append((event_index, name, km, price, quota, start))

# --- Registrations -----------------------------------------------------
participants = list(range(5, 21))
emergency_pool = [u[0] for u in users]

plan = [
    # (event_id, count, {status: n})
    (3, 14, {"CONFIRMED": 10, "PENDING_PAYMENT": 3, "CANCELLED": 1}),
    (2, 8, {"CONFIRMED": 6, "PENDING_PAYMENT": 1, "CANCELLED": 1}),
    (4, 7, {"CONFIRMED": 6, "PENDING_PAYMENT": 1}),
    (1, 11, {"CONFIRMED": 8, "PENDING_PAYMENT": 2, "CANCELLED": 1}),
    (7, 9, {"FINISHED": 7, "DISQUALIFIED": 1, "CHECKED_IN": 1}),
    (9, 5, {"FINISHED": 5}),
    (10, 5, {"CONFIRMED": 3, "PENDING_PAYMENT": 1, "CANCELLED": 1}),
]

registrations = []
reg_seq = 1
bib_seq = 10001
for event_id, count, statuses in plan:
    status_list = []
    for status, n in statuses.items():
        status_list += [status] * n
    assert len(status_list) == count
    random.shuffle(status_list)

    cats = [c for c in category_rows if c[0] == event_id]
    ev = events[event_id - 1]
    reg_open = date.fromisoformat(ev[10])
    reg_close = date.fromisoformat(ev[11])
    horizon = min(reg_close, date(2026, 11, 14))
    used_users = set()
    for i in range(count):
        status = status_list[i]
        participant = participants[(event_id * 7 + i * 3) % len(participants)]
        while participant in used_users:
            participant = participants[(participants.index(participant) + 1) % len(participants)]
        used_users.add(participant)
        cat = cats[i % len(cats)]
        days = max((horizon - reg_open).days, 1)
        reg_day = reg_open + timedelta(days=random.randint(0, days))
        reg_ts = datetime(reg_day.year, reg_day.month, reg_day.day,
                          random.randint(8, 22), random.randint(0, 59), random.randint(0, 59))
        bib = None
        checked_in = None
        cancelled = None
        if status in ("CONFIRMED", "FINISHED", "DISQUALIFIED"):
            bib = "BIB" + str(bib_seq)
            bib_seq += 1
        elif status == "CHECKED_IN":
            bib = "BIB" + str(bib_seq)
            bib_seq += 1
            checked_in = f"{ev[8]} 05:45:00"
        elif status == "CANCELLED":
            cancelled = (reg_ts + timedelta(days=2)).strftime("%Y-%m-%d %H:%M:%S")

        registrations.append({
            "code": f"RE-{reg_day.year}-{reg_seq:04d}", "user": participant, "cat": cat,
            "event": event_id, "bib": bib, "status": status, "amount": cat[3],
            "registered": reg_ts.strftime("%Y-%m-%d %H:%M:%S"),
            "checked_in": checked_in, "cancelled": cancelled,
            "jersey": random.choice(["S", "M", "L", "XL"]),
            "emergency_name": emergency_pool[(participant + 2) % len(emergency_pool)],
            "emergency_phone": users[(participant + 2) % len(users)][2],
        })
        reg_seq += 1

# --- Results for completed events -------------------------------------
results = []
for event_id in (7, 9):
    finished = [r for r in registrations if r["event"] == event_id and r["status"] == "FINISHED"]
    per_cat = {}
    for r in finished:
        base = 1500 if r["cat"][2] <= 5 else 2700
        r["time"] = base + random.randint(0, 900)
        per_cat.setdefault(r["cat"][0], []).append(r)
    for cat_id, rows in per_cat.items():
        rows.sort(key=lambda r: r["time"])
        for pos, r in enumerate(rows, start=1):
            r["cat_pos"] = pos
    finished.sort(key=lambda r: r["time"])
    for pos, r in enumerate(finished, start=1):
        r["overall_pos"] = pos
        results.append(r)

# --- Announcements ----------------------------------------------------
announcements = [
    (3, "Race kit collection starts 8 December", "Collect your race kit at the UiTM Shah Alam Grand Hall from 8 to 11 December, 10:00 AM to 8:00 PM. Bring your confirmation e-mail and MyKad. Proxy collection is allowed with a signed authorisation letter.", "ALL", 3, "2026-11-10 09:00:00"),
    (3, "Neon paint station and bag drop", "Bag drop opens at 4:00 PM at the start pen. Neon paint stations are free for all participants. Please wear light-coloured clothing for the best glow effect.", "CONFIRMED", 3, "2026-11-12 15:30:00"),
    (1, "Early bird registration extended", "Early bird pricing for the Penang Bridge International Marathon 2027 has been extended to 30 November 2026. Register now to secure your slot and limited-edition finisher tee.", "ALL", 2, "2026-11-08 11:00:00"),
    (7, "Official results and e-certificates released", "The official results for Melaka Heritage Run 2026 are now available. Log in and open My Registrations to view your finish time, pace, position and e-certificate.", "ALL", 2, "2026-10-06 18:00:00"),
    (10, "Pacers confirmed for December time trial", "Pacing groups for 20, 25, 30, 35 and 40 minutes are confirmed. Arrive 30 minutes early for warm-up and bib collection.", "ALL", 3, "2026-11-13 10:00:00"),
]

# --- Audit log --------------------------------------------------------
audits = [
    (1, "LOGIN", "USER", "1", "Administrator signed in", "2026-11-14 08:02:11"),
    (2, "CREATE", "EVENT", "1", "Created event Penang Bridge International Marathon 2027", "2026-09-28 10:15:00"),
    (1, "APPROVE", "EVENT", "1", "Approved event Penang Bridge International Marathon 2027", "2026-09-29 09:12:00"),
    (3, "CREATE", "EVENT", "3", "Created event UiTM Neon Fun Run 2026", "2026-10-12 14:20:00"),
    (1, "APPROVE", "EVENT", "3", "Approved event UiTM Neon Fun Run 2026", "2026-10-13 11:05:00"),
    (1, "EXPORT", "REPORT", "7", "Exported participant report for Melaka Heritage Run 2026", "2026-10-05 16:42:00"),
    (2, "CHECK_IN", "REGISTRATION", "00001", "Checked in participant at Melaka Heritage Run 2026", "2026-10-04 05:41:00"),
    (1, "UPDATE", "USER", "14", "Changed account status to INACTIVE", "2026-11-02 13:30:00"),
    (1, "REJECT", "EVENT", "8", "Rejected event Langkawi Coastal Run 2027", "2026-11-11 10:20:00"),
    (2, "CREATE", "ANNOUNCEMENT", "1", "Published race kit collection announcement", "2026-11-10 09:00:00"),
    (1, "LOGIN", "USER", "1", "Administrator signed in", "2026-11-15 07:55:40"),
    (2, "LOGIN", "USER", "2", "Organizer signed in", "2026-11-15 08:12:02"),
]

lines = []
lines.append("-- RunEase demonstration data (generated by database/generate_seed.py)\n")
lines.append("-- All accounts use the password: Password123!\n\n")

lines.append("-- Users\n")
for name, email, phone, role, status, gender, dob, city in users:
    lines.append(
        "INSERT INTO users (full_name, email, password_hash, phone, role, status, gender, date_of_birth, city, last_login_at, created_at) VALUES ("
        f"{q(name)}, {q(email)}, {q(PWD)}, {q(phone)}, {q(role)}, {q(status)}, {q(gender)}, {q(dob)}, {q(city)}, "
        f"TIMESTAMP '2026-11-14 09:00:00', TIMESTAMP '2026-09-01 08:00:00');\n")
lines.append("\n-- Organizer profiles\n")
for uid, org, reg_no, contact, web, status, verified, by in organizers:
    lines.append(
        "INSERT INTO organizers (user_id, organization_name, registration_no, contact_person, website, verification_status, verified_at, verified_by, created_at) VALUES ("
        f"{uid}, {q(org)}, {q(reg_no)}, {q(contact)}, {q(web)}, {q(status)}, "
        + (f"TIMESTAMP '{verified}', {by}" if verified else "NULL, NULL")
        + ", TIMESTAMP '2026-09-05 09:00:00');\n")
lines.append("\n-- Events\n")
for (title, slug, org_id, etype, desc, venue, city, state, edate, flag, ropen, rclose, status, rejection, _cats) in events:
    lines.append(
        "INSERT INTO events (organizer_id, title, slug, description, event_type, venue, city, state, event_date, flag_off_time, registration_open, registration_close, status, rejection_reason, created_at) VALUES ("
        f"{org_id}, {q(title)}, {q(slug)}, {q(desc)}, {q(etype)}, {q(venue)}, {q(city)}, {q(state)}, "
        f"DATE '{edate}', TIME '{flag}', DATE '{ropen}', DATE '{rclose}', {q(status)}, {q(rejection)}, TIMESTAMP '2026-09-10 09:00:00');\n")
lines.append("\n-- Event categories\n")
for event_id, name, km, price, quota, start in category_rows:
    lines.append(
        "INSERT INTO event_categories (event_id, name, distance_km, price, quota, start_time) VALUES ("
        f"{event_id}, {q(name)}, {km}, {price}, {quota}, TIME '{start}');\n")

lines.append("\n-- Registrations\n")
for r in registrations:
    lines.append(
        "INSERT INTO registrations (registration_code, user_id, category_id, bib_number, jersey_size, emergency_name, emergency_phone, medical_notes, status, total_amount, registered_at, checked_in_at, cancelled_at) VALUES ("
        f"{q(r['code'])}, {r['user']}, {r['cat'][0]}, {q(r['bib'])}, {q(r['jersey'])}, {q(r['emergency_name'])}, {q(r['emergency_phone'])}, NULL, "
        f"{q(r['status'])}, {r['amount']}, TIMESTAMP '{r['registered']}', "
        + (f"TIMESTAMP '{r['checked_in']}'" if r["checked_in"] else "NULL") + ", "
        + (f"TIMESTAMP '{r['cancelled']}'" if r["cancelled"] else "NULL") + ");\n")

lines.append("\n-- Payments (one per registration)\n")
for i, r in enumerate(registrations, start=1):
    if r["status"] in ("CONFIRMED", "CHECKED_IN", "FINISHED", "DISQUALIFIED"):
        pay_status, paid_at = "PAID", r["registered"]
    elif r["status"] == "CANCELLED":
        pay_status, paid_at = "REFUNDED", r["registered"]
    else:
        pay_status, paid_at = "PENDING", None
    ref = f"PAY-2026-{i:05d}"
    method = ["FPX", "CARD", "EWALLET"][i % 3]
    lines.append(
        "INSERT INTO payments (registration_id, reference_no, method, amount, status, paid_at, created_at) VALUES ("
        f"{i}, {q(ref)}, {q(method)}, {r['amount']}, {q(pay_status)}, "
        + (f"TIMESTAMP '{paid_at}'" if paid_at else "NULL") + ", "
        f"TIMESTAMP '{r['registered']}');\n")

lines.append("\n-- Results\n")
reg_lookup = {r["code"]: i for i, r in enumerate(registrations, start=1)}
for r in results:
    rid = reg_lookup[r["code"]]
    pace = round(r["time"] / float(r["cat"][2]))
    lines.append(
        "INSERT INTO results (registration_id, finish_time_seconds, overall_position, category_position, pace_seconds, recorded_by, recorded_at) VALUES ("
        f"{rid}, {r['time']}, {r['overall_pos']}, {r['cat_pos']}, {pace}, 2, TIMESTAMP '2026-10-04 12:00:00');\n")

lines.append("\n-- Announcements\n")
for event_id, title, body, audience, author, published in announcements:
    lines.append(
        "INSERT INTO announcements (event_id, title, body, audience, created_by, published_at) VALUES ("
        f"{event_id}, {q(title)}, {q(body)}, {q(audience)}, {author}, TIMESTAMP '{published}');\n")

lines.append("\n-- Audit trail\n")
for uid, action, entity, entity_id, desc, created in audits:
    lines.append(
        "INSERT INTO audit_logs (user_id, action, entity_type, entity_id, description, ip_address, created_at) VALUES ("
        f"{uid}, {q(action)}, {q(entity)}, {q(entity_id)}, {q(desc)}, '127.0.0.1', TIMESTAMP '{created}');\n")

sql = "".join(lines)
targets = [
    Path(__file__).resolve().parent.parent / "src/main/resources/db/seed.sql",
    Path(__file__).resolve().parent / "seed.sql",
]
for target in targets:
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(sql, encoding="utf-8")
print(f"seed.sql written: {len(registrations)} registrations, {len(results)} results, {len(events)} events")
