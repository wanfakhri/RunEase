# Deploying RunEase to a web server

This guide publishes RunEase to a public URL so anyone can open it from their own
laptop — no need to keep your computer or `run.ps1` running.

The project includes a production-ready `Dockerfile` that:

1. builds `target/runease.war` with Maven,
2. deploys it to Tomcat 10.1 as the root context (`/`), and
3. maps the platform's `PORT` variable onto Tomcat's HTTP connector.

The WAR now initialises its own database through `AppContextListener`, so the
schema and demonstration data are created on the first start.

---

## Option 1 — Render (recommended, free)

Render offers free Docker web services and only needs a GitHub account.

### Step 1: put the project on GitHub

Git is not installed on this laptop, so use **GitHub Desktop**:

1. Install GitHub Desktop from <https://desktop.github.com/> and sign in.
2. **File → Add local repository** and choose the project folder
   (`...\Nexus CSC584`).
3. When prompted that it is not a Git repository, click **create a repository**.
4. Commit the files (the included `.gitignore` keeps `target/`, `data/` and
   caches out of the repository).
5. Click **Publish repository** and untick *Keep this code private* if you want
   the simplest setup (either visibility works with Render).

### Step 2: create the web service

1. Sign in at <https://render.com> with GitHub.
2. Click **New → Blueprint** (the included `render.yaml` is detected), or
   **New → Web Service** and pick the repository.
3. If configuring manually, use:

   | Setting | Value |
   | --- | --- |
   | Language / Runtime | **Docker** |
   | Branch | `main` |
   | Region | Singapore (closest to Malaysia) |
   | Instance type | **Free** |
   | Health check path | `/` |

4. Click **Create Web Service** / **Deploy**.

The first build takes roughly 5–10 minutes. When it finishes, Render shows a URL
such as:

```
https://runease-xxxx.onrender.com
```

Share that link — your friend can open it from any laptop, on any network.

### Free-plan behaviour

- The service **sleeps after 15 minutes of inactivity**. The next visit takes
  about 30–60 seconds to wake up.
- The container filesystem is **temporary**. The H2 database is recreated with
  the demonstration data whenever the service restarts or redeploys, so
  registrations made by friends do not survive a restart. This is fine for a
  class demonstration.
- If you need persistence, either attach a paid Render disk mounted at
  `/usr/local/tomcat/data`, or point the app at a free MySQL instance:

  | Variable | Example |
  | --- | --- |
  | `RUNEASE_DB_URL` | `jdbc:mysql://host:3306/runease?useSSL=true` |
  | `RUNEASE_DB_USER` | `runease` |
  | `RUNEASE_DB_PASSWORD` | `your-password` |

  Import `database/schema_mysql.sql` and `database/seed_mysql.sql` first.

---

## Option 2 — Railway

Railway detects the `Dockerfile` automatically.

1. Push the project to GitHub as in Option 1.
2. Sign in at <https://railway.app> with GitHub.
3. **New Project → Deploy from GitHub repo** and select the repository.
4. Railway builds the image and assigns a public domain under
   **Settings → Networking → Generate Domain**.
5. Optional: add a **Volume** mounted at `/usr/local/tomcat/data` to keep the
   H2 database between deployments.

Railway includes a small free trial credit, after which a paid plan is needed.

---

## Option 3 — Azure App Service (free F1 tier, WAR deployment)

Use this if you have an Azure for Students subscription and prefer a managed
Tomcat host instead of Docker.

1. Create an **App Service** with:
   - Publish: **Code**
   - Runtime stack: **Java 21**
   - Java web server stack: **Tomcat 10.1**
   - Pricing plan: **Free F1**
2. Build the WAR locally:
   ```powershell
   .\run.ps1 -Package
   ```
3. Deploy `target\runease.war` using either:
   - the **Azure App Service** extension in VS Code (**Deploy to Web App**), or
   - **Deployment Center → FTPS / Local Git**, or
   - the CLI:
     ```powershell
     az webapp deploy --resource-group <group> --name <app> --src-path target\runease.war --type war
     ```
4. Open `https://<app>.azurewebsites.net`.

The free F1 plan has a daily CPU quota and may be paused when exhausted.

---

## Option 4 — Your own VPS (full control)

Any small Linux server with Java 21 works:

```bash
# on the server
sudo apt update && sudo apt install -y openjdk-21-jre
# install Tomcat 10.1 into /opt/tomcat, then:
sudo cp runease.war /opt/tomcat/webapps/ROOT.war
sudo /opt/tomcat/bin/startup.sh
```

Put Nginx or Caddy in front for HTTPS, and point `RUNEASE_DB_URL` at MySQL for
persistent data. This is the only option that gives you a normal, always-on
server, but it is not free.

---

## What NOT to use

- **Netlify, Vercel, GitHub Pages, InfinityFree** — these host static files or
  PHP only. They cannot run Jakarta Servlets/JSP.
- **Sending your LAN IP** (`http://172.20.10.2:8080`) — only works while your
  laptop is on and both devices share the same network.
- **A tunnel while your laptop sleeps** — the URL dies with your machine.

---

## Verifying a deployment

After the host gives you a URL, open it and check:

- `/` shows the RunEase landing page.
- `/events` lists the seeded events.
- Sign in with `admin@runease.my` / `Password123!`.
- `/admin` loads the dashboard.

You can also run the smoke test against the public URL:

```powershell
.\scripts\smoke-test.ps1 -BaseUrl https://runease-xxxx.onrender.com
```

> The demo accounts use a published password. For a real deployment, change
> them or remove the seeded accounts before sharing the URL widely.
