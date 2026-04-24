// Script to setup the issuer DID

"use strict";

const FS = require("fs");

const { resolveUrl, doPost, pemToBase64 } = require("./utils.js");

const issuerPublicKeyFile = process.env.ISSUER_PUBLIC_KEY_FILE || './issuer-public.pem';

const identityProxyUrl = process.env.IDENTITY_PROXY_URL || 'http://127.0.0.1:8080/';

const identityProxyAuthHeaders = { 'x-api-token': process.env.IDENTITY_PROXY_API_TOKEN || 'change_me_1' };

const issuerWalletAddress = process.env.ISSUER_WALLET_ADDRESS || "0xed088cb405441491c0d47dd9d7935671bd12cb24"

async function updateIssuerDid() {
    const result = await doPost(resolveUrl("./api/v1/indy/" + issuerWalletAddress, identityProxyUrl), {
        "configuration": {
            "publicKeys": [
                {
                    "type": "Ed25519Signature2018",
                    "base64": pemToBase64(FS.readFileSync(issuerPublicKeyFile).toString()),
                    "purposes": {
                        "authentication": true,
                        "assertionMethod": true,
                        "keyAgreement": true,
                        "capabilityInvocation": true,
                        "capabilityDelegation": true
                    },
                }
            ],
            "services": []
        },
        "txSign": {
            "mode": "private_key",
            "privateKey": process.env.ISSUER_WALLET_PRIVATE_KEY || "71dba676c3a978d1c49f86255ec21db655f8aaafe7392626ca121d037883ef65",
        },
    }, identityProxyAuthHeaders);

    if (!(result.sendResult?.success || result.success)) {
        throw new Error(result.sendResult?.error || result.error || "Unknown error");
    }
}

async function main() {
    // Update issuer's DID

    let done = false;

    while (!done) {
        try {
            await updateIssuerDid();
            done = true;
        } catch (ex) {
            console.log("Error: " + ex.message + ". " + "Retrying in 2 seconds...");
            await new Promise((resolve) => setTimeout(resolve, 2000));
        }
    }
}

main().then(() => {
    console.log("Done!");
    process.exit(0);
}).catch(err => {
    console.error(err);
    process.exit(1);
});