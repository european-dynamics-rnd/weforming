package com.ed.edc_handler.controller.asset;

import com.ed.edc_handler.model.FileEntity;
import com.ed.edc_handler.service.asset.AssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/asset")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping(path = "/provide", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map provideData(@RequestBody Map<String, Map<String, Object>> parameters,
                                       @RequestHeader Map<String, String> headers) {
        log.debug("*** Provide Data Called");
        log.debug("body= " + parameters.toString());
        log.debug("headers= "+headers.toString());
        return this.assetService.provideData(parameters, headers);
    }

    @PostMapping(path = "/consume", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map consumeData(@RequestBody Map<String, String> parameters,
                                  @RequestHeader Map<String, String> headers) {
        log.debug("*** Consume Data Called");
        log.debug("body= " + parameters.toString());
        log.debug("headers= "+headers.toString());
        return this.assetService.consumeData(parameters, headers);
    }

    @GetMapping(path = "/my-assets", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map getMyAssets(@RequestParam(name = "page", required = false) int page) {
        return this.assetService.getMyAssets(page);
    }

}
