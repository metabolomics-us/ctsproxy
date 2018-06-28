package edu.ucdavis.fiehnlab.ctsRest.client;

import edu.ucdavis.fiehnlab.config.CtsClientConfiguration;
import edu.ucdavis.fiehnlab.ctsRest.model.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by diego on 2/16/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CtsClientConfiguration.class)
@EnableFeignClients
public class CtsClientTest {
	private String inchikey = "MYMOFIZGZYHOMD-UHFFFAOYSA-N";
	private String inchiCode = "InChI=1S/O2/c1-2";
	private String molDef = "  2  1  0  0  0  0  0  0  0  0999 V2000\n" +
			"    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
			"    1.2990   -0.7500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\n" +
			"  2  1  2  0  0  0  0 \n" +
			"M  END";
	private String fullMol = " 977\\n" +
			"  -OEChem-07071413282D\\n\\n" +
			"  2  1  0  0  0  0  0  0  0  0999 V2000\\n" +
			"    0.0000    0.0000    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\\n" +
			"    1.2990   -0.7500    0.0000 O   0  0  0  0  0  0  0  0  0  0  0  0\\n" +
			"  2  1  2  0  0  0  0\\n" +
			"M  END";

    @LocalServerPort
    int port;

	@Autowired
	private CtsClient client;


	@Before
	public void setUp() {
		System.out.println("Client config: " + client.toString());
	}

	@After
	public void tearDown() {
		System.out.println("--------------------------------------------------------");
	}

	@Test
	public void testFromValues() {
		List<String> res = client.getSourceIdNames();

		assertNotNull(res);
		assertTrue(res.size() >= 10);
		assertTrue(res.contains("Chemical Name"));
		assertTrue(res.contains("Human Metabolome Database"));
		assertTrue(res.contains("InChIKey"));
		assertTrue(res.contains("KEGG"));
		assertTrue(res.contains("PubChem CID"));
		assertTrue(res.contains("ChemSpider"));
	}

	@Test
	public void testToValues() {
		List<String> res = client.getTargetIdNames();

		assertNotNull(res);
		assertTrue(res.size() >= 10);
		assertTrue(res.contains("Chemical Name"));
		assertTrue(res.contains("Human Metabolome Database"));
		assertTrue(res.contains("InChI Code"));
		assertTrue(res.contains("InChIKey"));
		assertTrue(res.contains("KEGG"));
		assertTrue(res.contains("PubChem CID"));
		assertTrue(res.contains("ChemSpider"));
	}

	@Test
	public void testChemifyQuery() {
		String res = client.chemifyQuery("alanine");

		assertNotNull(res);
	}

	@Test
	public void testBiologicalCount() {
		Map<String, Integer> res = client.compoundBiologicalCount(inchikey);

		assertNotNull(res);
		assertTrue(res.keySet().size() > 1);

		int sum = 0;
		for (Map.Entry<String, Integer> item : res.entrySet()) {
			assertTrue(item.getValue() > 0);
			if (!item.getKey().equals("total")) {
				sum += item.getValue();
			}
		}

		assertEquals(sum, res.get("total").intValue());
	}

	@Test
	public void testExtidCount() {
		ExtidCountResponse res = client.compoundExtidCount(inchikey);

		assertNotNull(res);
		assertTrue(res.datasource_count > 100);
	}

	@Test
	public void testFormulaExpansion() {
		FormulaResponse res = client.expandFormula("C8H10N4O2");

		assertNotNull(res);
		assertNull(res.error);
		assertEquals("CCCCCCCCHHHHHHHHHHNNNNOO", res.result);
	}

	@Test
	public void testCompoundInfo() {
		SynonymDto syn = new SynonymDto("Oxygen", "Synonym", 0);
		ExtidDto extid = new ExtidDto("PubChem CID", "977", "https://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi");

		CompoundResponse res = client.compoundInfo(inchikey);

		assertNotNull(res);
		assertEquals(inchikey, res.inchikey);
		assertTrue(res.synonyms.contains(syn));
		assertTrue(res.externalIds.contains(extid));
	}

	@Test
	public void testInchi2Mol() throws UnsupportedEncodingException {
		String inchicode = "{\"inchicode\":\"" + inchiCode + "\"}";

		MoleculeResponse res = client.inchi2Mol(inchicode);

		assertNotNull(res);
		assertNull(res.message);
		assertTrue(res.molecule.trim().endsWith(molDef.trim()));
	}

	@Test
	public void testInchikey2Mol() {
		MoleculeResponse res = client.inchiKey2Mol(inchikey);

		assertNotNull(res);
		assertNull(res.message);
		assertTrue(res.molecule.trim().endsWith(molDef.trim()));
	}

	@Test
	public void testMol2Inchi() {
		InchiPairResponse res = client.mol2Inchi("{\"mol\":\"" + fullMol + "\"}");

		assertNotNull(res);
		assertEquals(inchikey, res.inchikey);
		assertEquals(inchiCode, res.inchicode);
	}

	@Test
	public void testSmiles2Inchi() {
		String smiles = "{\"smiles\":\"[Cu+2].[O-]S(=O)(=O)[O-]\"}";

		String res = client.smiles2Inchi(smiles);

		assertNotNull(res);
		assertTrue(res.contains("\"inchicode\": \"InChI=1S/Cu.H2O4S/c;1-5(2,3)4/h;(H2,1,2,3,4)/q+2;/p-2\""));
	}

	@Test
	public void testInchiCode2InchiKey() {
		String inchicode = "{\"inchicode\":\"" + inchiCode + "\"}";

		Code2KeyResponse res = client.inchiCode2InchiKey(inchicode);

		assertNotNull(res);
		assertEquals(inchikey, res.inchikey);
	}

	@Test
	public void testConversion() {
		List<ConversionResult> res = client.convert("chemical name", "pubchem cid", "alanine");

		assertNotNull(res);
		assertTrue(res.size() > 0);
		assertArrayEquals(new String[]{"5950", "7311724", "57383916", "602", "71080", "7311725", "51283"}, res.get(0).results.toArray());
	}
}

