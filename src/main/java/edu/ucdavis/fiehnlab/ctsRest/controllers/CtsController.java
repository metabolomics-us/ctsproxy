package edu.ucdavis.fiehnlab.ctsRest.controllers;

import edu.ucdavis.fiehnlab.ctsRest.client.CtsClient;
import edu.ucdavis.fiehnlab.ctsRest.model.ConversionResult;
import edu.ucdavis.fiehnlab.ctsRest.model.FormulaResponse;
import edu.ucdavis.fiehnlab.ctsRest.model.MoleculeResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Created by diego on 2/15/2017.
 */
@RestController
@EnableFeignClients
@RequestMapping(path = "/rest")
public class CtsController {

    @Autowired
    CtsClient client;

    @Cacheable("simple_convert")
    @GetMapping("/convert/{from}/{to}/{searchTerm}")
    public List<ConversionResult> convertSimple(@PathVariable String from, @PathVariable String to, @PathVariable String searchTerm) {
        return client.convert(from, to, searchTerm);
    }

    @Cacheable("expand_formula")
    @RequestMapping(path = "/expandformula/{formula}")
    public FormulaResponse expandFormula(@PathVariable String formula) {
        return client.expandFormula(formula);
    }

    @Cacheable("from_values")
    @RequestMapping(path = "/fromValues")
    public List<String> fromValues() {
        return client.getSourceIdNames();
    }

    @Cacheable("to_values")
    @RequestMapping(path = "/toValues")
    public List<String> toValues() {
        return client.getTargetIdNames();
    }

    @Cacheable("bio_count")
    @RequestMapping(path = "/countBiological/{inchikey}", method = RequestMethod.GET)
    public Map<String, Integer> compoundBiologicalCount(@PathVariable("inchikey") String inchikey) {
        return client.compoundBiologicalCount(inchikey);
    }

    @Cacheable("scoring")
    @RequestMapping(path = "/score/{from}/{value}/{algorithm}", method = RequestMethod.GET)
    public String score(@PathVariable("from") String from, @PathVariable("value") String to, @PathVariable("algorithm") String algorithm) {
        return client.score(from, to, algorithm);
    }

    @Cacheable("inchikey_mol")
    @RequestMapping(path = "/inchikeytomol/{inchikey}", method = RequestMethod.GET)
    public MoleculeResponse inchiKey2Mol(@PathVariable("inchikey") String inchikey) {
        return client.inchiKey2Mol(inchikey);
    }

}
