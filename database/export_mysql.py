"""Export the RunEase schema and seed data as MySQL 8 scripts.

The application schema (src/main/resources/db/schema.sql) is written in H2/MySQL
compatible SQL. Two small differences remain for a pure MySQL load:

  1. H2 accepts typed literals such as TIMESTAMP '2026-11-14 09:00:00'.
     MySQL expects plain strings:        '2026-11-14 09:00:00'.
  2. MySQL benefits from an explicit engine and character set per table.

Run:  python database/export_mysql.py
"""

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SCHEMA_SRC = ROOT.parent / "src" / "main" / "resources" / "db" / "schema.sql"
SEED_SRC = ROOT / "seed.sql"

SCHEMA_OUT = ROOT / "schema_mysql.sql"
SEED_OUT = ROOT / "seed_mysql.sql"

DROP_ORDER = [
    "audit_logs",
    "announcements",
    "results",
    "payments",
    "registrations",
    "event_categories",
    "events",
    "organizers",
    "users",
]


def header(title: str) -> str:
    return (
        "-- =====================================================================\n"
        f"--  RunEase - Running Event Management System\n"
        f"--  {title}\n"
        "--  Target: MySQL 8.0+ (InnoDB, utf8mb4)\n"
        "-- =====================================================================\n\n"
        "SET NAMES utf8mb4;\n"
        "SET time_zone = '+08:00';\n\n"
        "CREATE DATABASE IF NOT EXISTS runease\n"
        "    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\n"
        "USE runease;\n\n"
    )


def convert_schema() -> str:
    body = SCHEMA_SRC.read_text(encoding="utf-8")

    # Strip the original H2 header comment block (first run of comment lines).
    body = re.sub(r"\A(--[^\n]*\n)+\n?", "", body)

    body = body.replace(
        "-- =====================================================================\n"
        "--  Indexes (query paths used by listing, filtering and reporting)\n"
        "-- =====================================================================",
        "-- =====================================================================\n"
        "--  Indexes (query paths used by listing, filtering and reporting)\n"
        "-- =====================================================================",
    )

    # Add engine + charset to every CREATE TABLE statement only.
    chunks = body.split(";")
    for i, chunk in enumerate(chunks):
        if re.search(r"CREATE\s+TABLE", chunk, re.IGNORECASE):
            chunks[i] = (
                chunk.rstrip()
                + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            )
    body = ";".join(chunks)

    drops = "\n".join(f"DROP TABLE IF EXISTS {table};" for table in DROP_ORDER)
    prelude = (
        "SET FOREIGN_KEY_CHECKS = 0;\n"
        f"{drops}\n"
        "SET FOREIGN_KEY_CHECKS = 1;\n\n"
    )

    return header("Relational schema (3NF) - MySQL export") + prelude + body.strip() + "\n"


def convert_seed() -> str:
    text = SEED_SRC.read_text(encoding="utf-8")
    # TIMESTAMP '2026-11-14 09:00:00' -> '2026-11-14 09:00:00'
    text = re.sub(r"\b(?:TIMESTAMP|DATE|TIME)\s*'", "'", text)

    preamble = header("Demonstration data - MySQL export")
    preamble += (
        "-- Load this file once into a freshly created schema, or re-run it to\n"
        "-- reset the demonstration data. All accounts use the password Password123!\n\n"
        "SET FOREIGN_KEY_CHECKS = 0;\n"
        + "\n".join(f"TRUNCATE TABLE {table};" for table in DROP_ORDER)
        + "\nSET FOREIGN_KEY_CHECKS = 1;\n\n"
    )
    return preamble + text.strip() + "\n"


def main() -> None:
    SCHEMA_OUT.write_text(convert_schema(), encoding="utf-8")
    SEED_OUT.write_text(convert_seed(), encoding="utf-8")
    print("Wrote", SCHEMA_OUT)
    print("Wrote", SEED_OUT)


if __name__ == "__main__":
    main()
