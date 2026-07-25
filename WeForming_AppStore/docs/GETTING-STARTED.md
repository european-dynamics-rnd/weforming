# Welcome to the WeForming App Store

*Getting started and getting around the app store*

## What is it?

The WeForming App Store is similar to an app store for the phone, but the phone now is your **building**. 
Every user can manage multiple **houses** with different app installation capabilities,
browse a catalog of **apps**, and **install** the apps into a house.
Disclaimer: Installing an app into the house doesn't necessarily mean the code bits actually run in the house.
The app can run in the cloud elsewhere, but be associated with that building - so don't take the assignment of the apps to the buildings too strictly. From then on, that app runs for that building.

```
   📚  Browse apps   →   🏠  Pick one of your houses   →   ⬇️  Install   →   ✅  Done
```

There are basically two types of app store users:

- **Users** — Regular users wanting to browse and install apps onto their houses.
- **Developers** — Develop and **publish** their own apps to the catalog so others can install them.

---

## The general idea

```
        APPS                          YOUR HOUSES
   ┌──────────────┐                 ┌──────────────┐
   │  Some  app   │                 │   House 1    │
   │  Metering    │  ──install──▶   │   House 2    │
   │  …           │                 │   …          │
   └──────────────┘                 └──────────────┘
```

- Each **house** associated with an user has a location on a map and has a list of **platform capabilities** it supports (for example *Docker*, *Proxmox LXC*, *Web*).
- Each **app** needs one of those platforms - this is for the sake of simplicity. Different app versions are listed separately.
- You can only install an app onto a house that supports the app's platform. The
  store checks this for you and filters the apps according to the platforms.

---

## Your first 5 minutes (as an user)

1. **Sign in** with your email and password. You get the username and password at HSE Keycloak. The app store is integrated with it.
2. **Set up a house.** Open **My Houses**, click **Add House**, give it a name, click
   its location on the map, and tick the platforms it supports. 
3. **Pick your house** from the menu at the top — the app catalog then shows only
   the apps that can be installed.
4. **Install an app.** Click **Install** on an app, follow the short wizard (choose the
   house, fill in any settings it asks for), and click **Install**.
5. **Watch it finish.** You'll see *"Waiting for the installation to finish…"*, and then
   a success message. You can find all your installs under **My Apps**.

That's it — you've installed your first app. 🎉

> Want the full details? See the [User Manual](MANUAL.md).

---

## For app developers: what you need before you publish

To add your app to the store, click **Publish a New App** and fill in the form. Have
these ready first:

### ✅ Required

| You need… | What it is |
| --- | --- |
| **App name** | What users see on the app card. |
| **Platform type** | The environment your app runs on — *Docker* or *Web* (pick from the list; you can add a new type if needed). A house must support this platform to install your app. |
| **Install URL** | A web address (endpoint) the store will call to actually install your app. This is the heart of it — when a user installs, the store sends the install details here and your system does the work. |
| **A way to confirm completion** | After your system finishes installing, it must call back to the store to say *"done"* (or *"failed"*). The store gives you everything you need to do this — see below. |
**Please ask the argovolt team for help if you get lost along the way.**

### ➕ Optional (but recommended)

| You may add… | What it is |
| --- | --- |
| **Description** | A short summary of what the app does. |
| **App icon** | An image for the card. |
| **Version** | e.g. `1.0.0`. |
| **Developer link** | A URL to your website or profile, shown on the card. |
| **Price** | A display price (leave 0 for free). |
| **Installation parameters** | If your app needs settings from the user (an IP address, an API key, …), describe them as a small **JSON Schema** as in [JSON Forms](https://jsonforms.io/). The store turns it into a form automatically. Skip this if your app needs no settings. |

### How the install reaches you (in 3 steps)

```
  1. User installs  →  2. Store POSTs the details to your Install URL  →  3. You install it,
                                                                            then call back "done"
```

When a user installs your app, the store sends a POST to your **Install URL** containing
the user's settings **plus** two values it generates: an `install_uuid` (a unique id for
this install) and a `secret_key`. When your system has finished, it calls the store back
with those two values to mark the install **finished** (or **failed**). No login is
needed for that call — the secret is what authorizes it.

> A step-by-step version of this flow, with a diagram, is in **[install-flow.md](install-flow.md)**.

---

## In one sentence

**Users install apps onto their buildings; developers provide an app and an Install URL,
and the store connects the two and tracks the result.**
