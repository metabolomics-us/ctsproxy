package edu.ucdavis.fiehnlab.ctsrest.web.services

import com.typesafe.scalalogging.LazyLogging
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.silent.AtomContainer
import org.openscience.cdk.smiles.{SmilesGenerator, SmiFlavor}
import org.springframework.stereotype.Service

import java.io.StringReader

@Service
class CdkSmilesService extends LazyLogging {

  private val smilesGenerator = new SmilesGenerator(SmiFlavor.Canonical)

  def molToSmiles(molBlock: String): String = {
    val reader = new MDLV2000Reader(new StringReader(molBlock))
    try {
      val molecule = reader.read(new AtomContainer())
      smilesGenerator.create(molecule)
    } finally {
      reader.close()
    }
  }
}
