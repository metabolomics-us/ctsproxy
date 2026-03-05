package edu.ucdavis.fiehnlab.ctsrest.web.services

import com.typesafe.scalalogging.LazyLogging
import edu.ucdavis.fiehnlab.ctsrest.client.api.CtsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class SmilesConversionService extends LazyLogging {

  @Autowired
  val ctsService: CtsService = null

  @Autowired
  val cdkSmilesService: CdkSmilesService = null

  @Cacheable(Array("inchikey_to_smiles"))
  def inchikeyToSmiles(inchikey: String): String = {
    val molResponse = ctsService.inchiKey2Mol(inchikey)

    if (molResponse == null || molResponse.molecule == null || molResponse.molecule.trim.isEmpty) {
      throw new IllegalArgumentException(s"No MOL block found for InChIKey: $inchikey")
    }

    logger.debug(s"Converting InChIKey $inchikey to SMILES via MOL block")
    cdkSmilesService.molToSmiles(molResponse.molecule)
  }
}
