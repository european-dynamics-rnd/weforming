// Utils

"use strict";

const Axios = require("axios");

const SEPARATOR = "------------------------------------";

const AUTH_HEADERS = {};

function resolveUrl(path, base) {
    return new URL(path, base).toString();
}

function prettifyJson(data) {
    return JSON.stringify(data, null, 4);
}

async function handleRequestErrors(p) {
    let r;

    try {
        r = await p;
    } catch (error) {
        if (error.response) {
            console.log("ERROR (" + error.code + ") - STATUS: " + error.response.status + " / " + error.response.statusText);
            console.log(prettifyJson(error.response.data || null));
        } else {
            console.error(error.message);
        }
        throw error;
    }

    return r;
}

async function doGet(url, headers) {
    console.log(`GET ${url}`);
    console.log("");
    let response;

    try {
        response = await Axios.get(url, { headers: { ...AUTH_HEADERS, ...(headers || {}) } });
    } catch (ex) {
        if (error.response) {
            if (error.response.status === 404) {
                console.log("404 NOT FOUND");
                console.log(prettifyJson(error.response.data || null));
                return null;
            }

            console.log("ERROR (" + error.code + ") - STATUS: " + error.response.status + " / " + error.response.statusText);
            console.log(prettifyJson(error.response.data || null));
        } else {
            console.error(error);
        }

        throw ex;
    }

    console.log("200 OK");
    console.log(prettifyJson(response.data));
    console.log(SEPARATOR);
    return response.data;
}

async function doPost(url, body, headers) {
    console.log(`POST ${url}`);
    console.log(prettifyJson(body));
    console.log("");
    const response = await handleRequestErrors(Axios.post(url, body, { headers: { ...AUTH_HEADERS, ...(headers || {}) } }));
    console.log("200 OK");
    console.log(prettifyJson(response.data));
    console.log(SEPARATOR);
    return response.data;
}

async function doPut(url, body, headers) {
    console.log(`PUT ${url}`);
    console.log(prettifyJson(body));
    console.log("");
    const response = await handleRequestErrors(Axios.put(url, body, { headers: { ...AUTH_HEADERS, ...(headers || {}) } }));
    console.log("200 OK");
    console.log(prettifyJson(response.data));
    console.log(SEPARATOR);
    return response.data;
}

async function doDelete(url, headers) {
    console.log(`DELETE ${url}`);
    console.log("");
    const response = await handleRequestErrors(Axios.delete(url, { headers: { ...AUTH_HEADERS, ...(headers || {}) } }));
    console.log("200 OK");
    console.log(prettifyJson(response.data));
    console.log(SEPARATOR);
    return response.data;
}

function pemToBase64(pem) {
    pem = pem.split("\n").map(l => l.trim()).filter(l => !!l);
    pem.pop();
    pem.shift();
    return pem.join("");
}

function waitTime(ms) {
    return new Promise(resolve => {
        setTimeout(resolve, ms);
    });
}

module.exports = {
    SEPARATOR,
    resolveUrl,
    prettifyJson,
    doGet,
    doPost,
    doPut,
    doDelete,
    pemToBase64,
    waitTime,
};
