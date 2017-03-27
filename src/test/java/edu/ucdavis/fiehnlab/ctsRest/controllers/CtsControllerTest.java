package edu.ucdavis.fiehnlab.ctsRest.controllers;

import edu.ucdavis.fiehnlab.ctsRest.ApplicationTests;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.Console;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by diego on 2/15/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(value = "${fiehnlab.cts.config.name}")
@EnableFeignClients
public class CtsControllerTest extends ApplicationTests {
	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		for (HttpMessageConverter<?> hmc : converters) {
			if (hmc instanceof MappingJackson2HttpMessageConverter) {
				this.mappingJackson2HttpMessageConverter = hmc;
				break;
			}
		}

		assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void getIndex() throws Exception {
		mockMvc.perform(get("/").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().string(equalTo("CTS main page")));
	}

	@Test
	public void getSimpleConversion() throws Exception {
		MvcResult res = mockMvc.perform(MockMvcRequestBuilders.get("/rest/convert/chemical name/inchikey/ethanol").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].fromIdentifier").value("chemical name"))
				.andExpect(jsonPath("$[0].toIdentifier").value("inchikey"))
				.andExpect(jsonPath("$[0].searchTerm").value("ethanol"))
				.andExpect(jsonPath("$[0].result").value("LFQSCWFLJHTTHZ-UHFFFAOYSA-N")).andReturn();

		System.out.println(res.getResponse().getContentAsString());
	}

	@Test
	public void getFormulaExpansion() throws Exception {
		MvcResult mvcres = mockMvc.perform(MockMvcRequestBuilders.get("/rest/expandformula/C2H6O").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.result").value("CCHHHHHHO")).andReturn();

		System.out.println(mvcres.getResponse().getContentAsString());
	}

	@Test
	public void testGetFromValues() throws Exception {
		MvcResult mvcres = mockMvc.perform(MockMvcRequestBuilders.get("/rest/fromValues").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(224))).andReturn();

		System.out.println(mvcres.getResponse().getContentAsString());
	}

	@Test
	public void testGetToValues() throws Exception {
		MvcResult mvcres = mockMvc.perform(MockMvcRequestBuilders.get("/rest/toValues").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(225))).andReturn();

		System.out.println(mvcres.getResponse().getContentAsString());
	}

	@Test
	@Ignore("MockMvc doesn't have the X-Proxy-Cache header for this test to pass. Working fine in production")
	public void testSecondCallHitsCache() throws Exception {
		MvcResult res1 = mockMvc.perform(MockMvcRequestBuilders.get("/rest/convert/chemical name/inchikey/alanine").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		Assert.assertEquals("MISS", res1.getResponse().getHeaderValue("X-Proxy-Cache"));

		MvcResult res2 = mockMvc.perform(MockMvcRequestBuilders.get("/rest/convert/chemical name/inchikey/alanine").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andReturn();
		Assert.assertEquals("HIT", res2.getResponse().getHeaderValue("X-Proxy-Cache"));

	}


	private String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
