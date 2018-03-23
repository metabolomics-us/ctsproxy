package edu.ucdavis.fiehnlab.ctsRest.client;

import edu.ucdavis.fiehnlab.ctsRest.config.CtsClientAutoConfiguration;
import edu.ucdavis.fiehnlab.ctsRest.model.*;
import feign.Headers;
import feign.Param;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Map;

/**
 * Created by diego on 2/16/2017.
 */
@Service
@FeignClient(name = "${fiehnlab.cts.config.name}", url = "${fiehnlab.cts.config.url}", configuration = CtsClientAutoConfiguration.class)
public interface CtsClient {
    @RequestMapping(path = "/service/convert/{from}/{to}/{searchTerm}", method = RequestMethod.GET)
    List<ConversionResult> convert(@PathVariable("from") String from, @PathVariable("to") String to, @PathVariable("searchTerm") String searchTerm);

    @RequestMapping(path = "/service/score/{from}/{value}/{algorithm}", method = RequestMethod.GET)
    String score(@PathVariable("from") String from, @PathVariable("value") String value, @PathVariable("algorithm") String algorithm);

    @RequestMapping(path = "/service/inchikeytomol/{inchikey}", method = RequestMethod.GET)
    MoleculeResponse inchiKey2Mol(@PathVariable("inchikey") String inchikey);

    @RequestMapping(path = "/service/expandFormula/{formula}", method = RequestMethod.GET)
    FormulaResponse expandFormula(@PathVariable("formula") String formula);

    @RequestMapping(path = "/service/compound/{inchikey}", method = RequestMethod.GET)
    CompoundResponse compoundInfo(@PathVariable("inchikey") String inchikey);

    @RequestMapping(path = "/service/synonyms/{inchikey}", method = RequestMethod.GET)
    List<String> compoundSynonyms(@PathVariable("inchikey") String inchikey);

    @RequestMapping(path = "/service/count/{inchikey}", method = RequestMethod.GET)
    ExtidCountResponse compoundExtidCount(@PathVariable("inchikey") String inchikey);

    @RequestMapping(path = "/service/countBiological/{inchikey}", method = RequestMethod.GET)
    Map<String, Integer> compoundBiologicalCount(@PathVariable("inchikey") String inchikey);

//	@RequestMapping(path = "/chemify/rest/identify/{name}", method = RequestMethod.GET)
//	String chemifyQuery(@PathVariable("name") String name);

    @RequestMapping(path = "/service/conversion/fromValues", method = RequestMethod.GET)
    List<String> getSourceIdNames();

    @RequestMapping(path = "/service/conversion/toValues", method = RequestMethod.GET)
    List<String> getTargetIdNames();

    @RequestMapping(path = "/service/extidScore/{extidName}", method = RequestMethod.GET)
    ExtidScoreResponse getExtidCount(@PathVariable("extidName") String extidName);

    @RequestMapping(path = "/service/moltoinchi", method = RequestMethod.POST)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    InchiPairResponse mol2Inchi(String mol);

    @RequestMapping(path = "/service/inchitomol", method = RequestMethod.POST)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    MoleculeResponse inchi2Mol(@Param("inchicode") String inchicode);

    @RequestMapping(path = "/service/smilestoinchi", method = RequestMethod.POST)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    String smiles2Inchi(@Param("smiles") String smiles);

    @RequestMapping(path = "/service/inchicodetoinchikey", method = RequestMethod.POST)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    Code2KeyResponse inchiCode2InchiKey(@Param("inchicode") String inchicode);
}
