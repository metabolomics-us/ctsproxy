package edu.ucdavis.fiehnlab.ctsRest.services

import java.io.StringReader

import edu.ucdavis.fiehnlab.ctsRest.client.CtsClient
import org.openscience.cdk.interfaces._
import org.openscience.cdk.io.MDLV2000Reader
import org.openscience.cdk.silent.SilentChemObjectBuilder
import org.openscience.cdk.smiles._
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
  * Created by diego on 3/21/2018
  **/
@Service
class TranslationService() {
  println("Creating Translator")

  @Autowired
  val client: CtsClient = null

  def inchikey2smiles(inchikey: String, from: String): String = {
    val mol = from.toLowerCase match {
      case "inchikey" =>
        client.inchiKey2Mol(inchikey)
      case _ => client.inchi2Mol(client.convert(from, "inchi code", inchikey).get(0).results.get(0))
    }

    val strReader = new StringReader(mol.molecule)
    val mr = new MDLV2000Reader(strReader)
    val container=mr.read(SilentChemObjectBuilder.getInstance().newInstance(classOf[IAtomContainer]))

    AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(container)
    AtomContainerManipulator.removeHydrogens(container)
    CDKHydrogenAdder.getInstance(SilentChemObjectBuilder).addImplicitHydrogens(container)

    val sg = SmilesGenerator.absolute()

    sg.create(container)
  }

}
