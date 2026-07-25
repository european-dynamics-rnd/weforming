# WeForming App Store — User Manual

The WeForming App Store lets you browse applications, install them onto your
buildings ("houses"), and — if you are a developer — publish your own apps.

> **Administrators:** for installation, configuration, and operations, see the
> [Administrator Guide](ADMIN.md).

- [Who can do what](#who-can-do-what)
- [Signing in](#signing-in)
- [A quick tour](#a-quick-tour)
- [Your houses (assets)](#your-houses-assets)
- [Finding apps](#finding-apps)
- [Installing an app](#installing-an-app)
- [My Apps](#my-apps)
- [For developers: publishing an app](#for-developers-publishing-an-app)
- [For developers: how installation reaches your system](#for-developers-how-installation-reaches-your-system)
- [Tips & troubleshooting](#tips--troubleshooting)
- [Glossary](#glossary)

---

## Who can do what

There are two kinds of users:

| Capability | Regular user | Developer |
| --- | :---: | :---: |
| Browse & search apps | ✅ | ✅ |
| Manage your houses (add/edit/delete) | ✅ | ✅ |
| Install apps onto a house | ✅ | ✅ |
| See your installs in **My Apps** | ✅ | ✅ |
| **Publish** apps to the store | — | ✅ |
| Add new **platform types** | — | ✅ |

Your role is assigned by your administrator when your account is created. If you
believe you should be able to publish apps but can't, contact your administrator.

---

## Signing in

1. Open the app store in your browser.
2. On the **Sign in** screen, enter your **email** and **password**.
3. Click **Sign In**.

You'll land on the store home page. To sign out, click the **exit icon** at the
top-right of the navigation bar.

---

## A quick tour

The bar across the top of every page contains:

| Control | What it does |
| --- | --- |
| **Search box** | Find apps by name (press Enter). |
| **My Apps** | Apps you've installed, with their status. |
| **My Houses** | Add and edit your buildings, and place them on a map. |
| **House selector** (the home icon menu) | Choose your **active house**. |
| **Show app platform type** | Toggle a small badge on each app card showing its platform. |
| **Exit icon** | Sign out. |

### Your active house

The house selector (top-right, with a home icon) sets your **active house**. This
matters because the store only shows apps whose platform is supported by the active
house — so you always see what's actually installable there. Switch houses at any
time from this menu.

---

## Your houses (assets)

A "house" is a building you own or manage. Open **My Houses** from the top bar.

You'll see a **map** with a marker for each house, and a list beside it.

### Add a house

1. Click **Add House**.
2. Fill in:
   - **Name** — e.g. "Summer House" (required).
   - **Address** — free text.
   - **Latitude / Longitude** — type them in, **or click the location on the map**
     to set them automatically.
   - **Supported platforms** — tick the platform types this building supports
     (e.g. *Docker*, *Web*). This controls which apps can be installed here.
   - **Building photo** — optionally upload a picture of the building.
3. Click **Save**.

### Edit or delete a house

- In the list, click the **pencil** icon (or click the house's **marker on the map**)
  to edit it. Change any field and click **Save**.
- Click the **trash** icon to delete a house (you'll be asked to confirm).

> **Tip:** While editing, clicking anywhere on the map drops the location pin for
> that house. The map uses OpenStreetMap and needs an internet connection to load.

---

## Finding apps

The **home page** lists the apps available for your **active house**.

- Each **app card** shows the icon, name, description, developer link, version, and
  price. Turn on **Show app platform type** in the top bar to also see the platform
  badge (e.g. *Docker*).
- **Search** by typing in the top search box and pressing Enter.
- If you see *"No apps match the platforms available for …"*, the active house
  doesn't support any of the listed apps' platforms. Add the needed platform to the
  house in **My Houses**, or switch to a different house.

---

## Installing an app

Click **Install** on an app card to open the install wizard.

1. **Introduction** — a short summary of the steps.
2. **Select Asset** — choose which house to install onto. The store checks that the
   house supports the app's platform:
   - ✅ green message → compatible, you can continue.
   - ❌ red message → that house doesn't support this app's platform; pick another
     house or add the platform to the house first.
3. **Parameters** — if the app needs configuration, a form appears here (for example
   an *API key* or an *IP address*). Required fields must be filled and are validated
   as you type. If the app needs no parameters, this step says so.
4. **Install** — review the summary and click **Install**.

### Waiting for completion

Installation runs on the app provider's side, so after you click Install you'll see
**"Waiting for the installation to finish…"**. This can take a while. You can safely
leave the page — when you come back to that app's install screen, it resumes waiting.

When the provider finishes, the screen shows either:

- **Installation Successful** — done; click **Back to Store**.
- **Installation Failed** — something went wrong; click **Try Again** or **Back to Store**.

---

## My Apps

**My Apps** lists everything you've installed. Each card shows the app, the house it
was installed on, the version, and a **status** badge:

| Badge | Meaning |
| --- | --- |
| 🟡 **Installing…** | The install is in progress (waiting for the provider). |
| 🟢 **Finished** | Successfully installed. |
| 🔴 **Failed** | The provider reported the install failed. |

The parameters you entered during installation are shown under **Configuration**.

---

## For developers: publishing an app

Developers see a **Publish a New App** button on the home page. It opens a form:

| Field | What to put |
| --- | --- |
| **App name** | The name shown on the card (required). |
| **Description** | A short summary. |
| **Platform type** | The platform your app runs on (chosen from the catalog, e.g. *Docker* or *Web*). A house must support this platform to install your app. |
| **Price (EUR)** | Display price (0 for free). |
| **Version** | e.g. `1.0.0`. |
| **Developer link (URL)** | A link to your site/profile, shown on the card. |
| **Install URL** | The endpoint the store calls to actually install your app (see the next section). |
| **Installation parameters** | A **JSON Schema** describing the fields users fill in at install time. Rendered as a form automatically. Leave empty if none. |
| **App icon** | An image for the card. |

Click **Publish** and your app appears in the store immediately, attributed to you.

### The parameters form (JSON Schema)

Whatever JSON Schema you paste into **Installation parameters** becomes the form
users see in step 3 of the install wizard. Titles become field labels, `required`
fields are enforced, and `type`/`pattern` rules validate input. Example:

```json
{
  "type": "object",
  "properties": {
    "ip_address": {
      "type": "string",
      "title": "IP address",
      "pattern": "^([0-9]{1,3}[.]){3}[0-9]{1,3}$"
    }
  },
  "required": ["ip_address"]
}
```

### Adding a platform type

Platform types (Docker, Web, …) are a shared, extensible catalog. Developers can add
a new one; afterwards it can be selected on apps and ticked on houses. Ask your
administrator or use the platform-types option if you need a platform that isn't
listed yet.

---

## For developers: how installation reaches your system

When a user installs your app, the store does **not** install anything itself — it
hands the job to **your Install URL** and waits for you to report back.

1. The store collects the user's parameters, then **POSTs them to your Install URL**,
   adding two values: a unique **`install_uuid`** and a **`secret_key`** (plus a
   `confirm_url`). Example body:

   ```json
   {
     "ip_address": "192.168.1.50",
     "install_uuid": "6769f47a-…",
     "secret_key": "ix6sO2wo…",
     "confirm_url": "https://weforming-appstore.argovoltlabs.eu/api/installs/confirm"
   }
   ```

2. Your system performs the installation (for as long as it needs).

3. When done, **call back** to confirm — no login required, the secret authorizes it:

   ```bash
   # success (default):
   curl -X POST https://your-store/api/installs/confirm \
     -H "Content-Type: application/json" \
     -d '{"install_uuid":"…","secret_key":"…"}'

   # or report a failure:
   curl -X POST https://your-store/api/installs/confirm \
     -H "Content-Type: application/json" \
     -d '{"install_uuid":"…","secret_key":"…","success":false}'
   ```

The user's **Waiting…** screen then resolves to **Finished** or **Failed**.

> Keep the `secret_key` private — anyone holding it for a given `install_uuid` can
> mark that install complete, which is exactly why the callback needs no other auth.

A step-by-step walkthrough of this flow, with a diagram, is in **[install-flow.md](install-flow.md)**.

---

## Tips & troubleshooting

- **"No apps match the platforms available for …"** — your active house doesn't
  support any listed app's platform. Add the platform in **My Houses**, or switch
  houses.
- **Can't continue past *Select Asset*** — the chosen house doesn't support the app's
  platform. Pick a compatible house or add the platform to it.
- **The map is blank** — the map tiles need internet access; check your connection.
- **An install is stuck on *Installing…*** — the provider hasn't confirmed completion
  yet. It resolves automatically once they call back; you can leave and return.
- **No *Publish a New App* button** — your account isn't a developer account; contact
  your administrator.
- **Session expired** — if you're returned to the sign-in screen unexpectedly, just
  sign in again.

---

## Glossary

| Term | Meaning |
| --- | --- |
| **House / Asset** | A building you manage. Apps are installed onto a house. |
| **Active house** | The house currently selected in the top bar; the catalog is filtered to it. |
| **Platform type** | The kind of environment an app needs (e.g. Docker, Web). A house lists the platforms it supports. |
| **Install URL** | A developer's endpoint that the store calls to perform an installation. |
| **install_uuid / secret_key** | Identifiers the store sends with an install so the provider can confirm completion. |
| **Developer** | A user who can publish apps and add platform types. |
