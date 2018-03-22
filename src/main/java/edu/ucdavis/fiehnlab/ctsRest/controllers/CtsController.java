package edu.ucdavis.fiehnlab.ctsRest.controllers;

import edu.ucdavis.fiehnlab.ctsRest.client.CtsClient;
import edu.ucdavis.fiehnlab.ctsRest.model.*;
import edu.ucdavis.fiehnlab.ctsRest.services.TranslationService;
import feign.Param;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @Autowired
    TranslationService translator;


    @Cacheable("simple_convert")
    @RequestMapping(path = "/convert/{from}/{to}/{searchTerm}", method = RequestMethod.GET, produces = "application/json")
    public List<ConversionResult> convertSimple(@PathVariable String from, @PathVariable String to, @PathVariable String searchTerm) {
        searchTerm = searchTerm.replaceAll("/", "_");
        List<ConversionResult> result = null;

        if(to.toLowerCase().equals("smiles")) {
            List<String> smiles = new ArrayList<>();
            smiles.add(translator.inchikey2smiles(searchTerm, from));
            result = new ArrayList<>();
            result.add(new ConversionResult(from, to, searchTerm, smiles));
        } else {
            result = client.convert(from, to, searchTerm);
        }
        return result;
    }

    @Cacheable("expand_formula")
    @RequestMapping(path = "/expandformula/{formula}", method = RequestMethod.GET, produces = "application/json")
    public FormulaResponse expandFormula(@PathVariable String formula) {
        return client.expandFormula(formula);
    }

    @Cacheable("from_values")
    @ApiOperation(value = "Returns the available IDs to convert from.")
    @RequestMapping(path = "/fromValues", method = RequestMethod.GET, produces = "application/json")
    public List<String> fromValues() {
        return client.getSourceIdNames();
    }

    @CacheEvict(value = "to_values", allEntries = true)
    @Cacheable("to_values")
    @RequestMapping(path = "/toValues", method = RequestMethod.GET, produces = "application/json")
    public List<String> toValues() {
        List<String> values = client.getTargetIdNames();
        System.out.println(values);
        values.add("SMILES");
        System.out.println(values);
        return values;
    }

    @Cacheable("bio_count")
    @RequestMapping(path = "/countBiological/{inchikey}", method = RequestMethod.GET, produces = "application/json")
    public Map<String, Integer> compoundBiologicalCount(@PathVariable("inchikey") String inchikey) {
        return client.compoundBiologicalCount(inchikey);
    }

    @Cacheable("scoring")
    @RequestMapping(path = "/score/{from}/{keyword}/{algorithm}", method = RequestMethod.GET, produces = "application/json")
    public String score(@PathVariable String from, @PathVariable String keyword, @PathVariable String algorithm) {
        return client.score(from, keyword, algorithm);
    }

    @Cacheable("inchikey_mol")
    @RequestMapping(path = "/inchikeytomol/{inchikey}", method = RequestMethod.GET, produces = "application/json")
    public MoleculeResponse inchiKey2Mol(@PathVariable("inchikey") String inchikey) {
        return client.inchiKey2Mol(inchikey);
    }

    @Cacheable("cmpd_synonyms")
    @RequestMapping(path = "/synonyms/{inchikey}", method = RequestMethod.GET, produces = "application/json")
    public List<String> compoundSynonyms(@PathVariable("inchikey") String inchikey) {
        return client.compoundSynonyms(inchikey);
    }

    @Cacheable("extidCount")
    @RequestMapping(path = "/extidCount/{extidName}", method = RequestMethod.GET)
    public ExtidScoreResponse extidCount(@PathVariable("extidName") String extidName) {
        return client.getExtidCount(extidName);
    }

    /******* POST REQUESTS ********/

    @Cacheable("mol2inchi")
    @RequestMapping(path = "/moltoinchi", method = RequestMethod.POST, produces = "application/json")
    public InchiPairResponse mol2Inchi(String mol) {
        return client.mol2Inchi(mol);
    }

    @Cacheable("smiles2inchi")
    @RequestMapping(path = "/smilestoinchi", method = RequestMethod.POST, produces = "application/json")
    public String smiles2Inchi(@Param("smiles") String smilesCode) {
        return client.smiles2Inchi(smilesCode);
    }

    @Cacheable("inchi2inchikey")
    @RequestMapping(path = "/inchicodetoinchikey", method = RequestMethod.POST, produces = "application/json")
    public Code2KeyResponse inchiCode2InchiKey(@Param("inchicode") String inchi) {
        return client.inchiCode2InchiKey(inchi);
    }

    @Cacheable("inchi2mol")
    @RequestMapping(path = "/inchicodetomol", method = RequestMethod.POST, produces = "application/json")
    public MoleculeResponse inchiCode2Mol(@Param("inchicode") String inchi) {
        return client.inchi2Mol(inchi);
    }

    @CacheEvict(value = "from_values", allEntries = true)
    @GetMapping(path = "/refreshFromValues")
    public void refreshFromValues() {}

    @CacheEvict(value = "to_values", allEntries = true)
    @GetMapping(path = "/refreshToValues")
    public void refreshToValues() {
        System.out.println("Clearing to_values cache...");
    }
}
