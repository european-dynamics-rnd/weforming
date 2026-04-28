package main

import (
	"io"
	"net/http"
	"strings"
)

const (
	FILE_PERMISSION   = 0600 // Read/Write
	FOLDER_PERMISSION = 0700 // Read/Write/Run
)

func concatUrl(base string, path string) string {
	if (strings.HasSuffix(base, "/") && !strings.HasPrefix(path, "/")) || (!strings.HasSuffix(base, "/") && strings.HasPrefix(path, "/")) {
		return base + path
	} else if strings.HasSuffix(base, "/") && strings.HasPrefix(path, "/") {
		return base + path[1:]
	} else {
		return base + "/" + path
	}
}

func addressFromDid(did string) string {
	spl := strings.Split(did, ":")

	if len(spl) == 0 {
		return ""
	}

	return spl[len(spl)-1]
}

func pemToBase64(pem string) string {
	spl := strings.Split(pem, "\n")

	spl2 := make([]string, 0)

	for _, s := range spl {
		s = strings.TrimSpace(s)
		if len(s) == 0 {
			continue
		}

		spl2 = append(spl2, s)
	}

	if len(spl2) < 3 {
		return ""
	}

	return strings.Join(spl2[1:len(spl2)-1], "")
}

func doGetRequest(requestURL string, headers map[string]string) (int, string, error) {
	req, err := http.NewRequest(http.MethodGet, requestURL, nil)
	if err != nil {
		return 0, "", err
	}

	for key, value := range headers {
		req.Header.Set(key, value)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return 0, "", err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return resp.StatusCode, "", err
	}

	return resp.StatusCode, string(body), nil
}

func doPostRequest(requestURL string, headers map[string]string, requestBody string) (int, string, error) {
	req, err := http.NewRequest(http.MethodPost, requestURL, strings.NewReader(requestBody))
	if err != nil {
		return 0, "", err
	}

	for key, value := range headers {
		req.Header.Set(key, value)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return 0, "", err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return resp.StatusCode, "", err
	}

	return resp.StatusCode, string(body), nil
}

func doPutRequest(requestURL string, headers map[string]string, requestBody string) (int, string, error) {
	req, err := http.NewRequest(http.MethodPut, requestURL, strings.NewReader(requestBody))
	if err != nil {
		return 0, "", err
	}

	for key, value := range headers {
		req.Header.Set(key, value)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return 0, "", err
	}
	defer resp.Body.Close()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return resp.StatusCode, "", err
	}

	return resp.StatusCode, string(body), nil
}
