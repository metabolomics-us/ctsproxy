package edu.ucdavis.fiehnlab.ctsrest.web.services

import net.sf.jniinchi.INCHI_RET
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.inchi.{InChIGeneratorFactory, InChIToStructure}
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.smiles.{SmiFlavor, SmilesGenerator}
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.springframework.stereotype.{Component, Service}

@Component
class InChIConversionService {

  /**
   *
   * @param molecule
   * @return
   */
  private def addHydrogens(molecule: IAtomContainer, addExplicitHydrogens: Boolean = false): IAtomContainer = {
    if (molecule != null) {
      val newMolecule: IAtomContainer = molecule

      AtomContainerManipulator.removeHydrogens(newMolecule)
      AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(newMolecule)
      CDKHydrogenAdder.getInstance(DefaultChemObjectBuilder.getInstance()).addImplicitHydrogens(newMolecule)

      if (addExplicitHydrogens) {
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(newMolecule)
      }

      newMolecule
    } else {
      molecule
    }
  }

  /**
   *
   * @param molecule
   * @param flavor
   * @return
   */
  private def moleculeToSMILES(molecule: IAtomContainer, flavor: Int): String = {
    try {
      println(new SmilesGenerator(flavor).create(molecule))
      new SmilesGenerator(flavor).create(molecule)
    } catch {
      case e: Exception => new SmilesGenerator(SmiFlavor.Unique | SmiFlavor.UseAromaticSymbols).create(addHydrogens(molecule))
    }
  }

  /**
   *
   * @param inchi
   * @param flavor
   * @return
   */
  def inchiToSmiles(inchi: String, flavor: Int = SmiFlavor.Absolute): String = {
    // parse InChI string
    val inchiGeneratorFactory: InChIGeneratorFactory = InChIGeneratorFactory.getInstance()
    val inchiToStructure: InChIToStructure = inchiGeneratorFactory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance())

    val molecule: IAtomContainer = inchiToStructure.getAtomContainer
    val returnStatus = inchiToStructure.getReturnStatus
    println(inchi)
    println(returnStatus)
    println(INCHI_RET.OKAY)
    println(INCHI_RET.WARNING)

    if (returnStatus == INCHI_RET.OKAY || returnStatus == INCHI_RET.WARNING) {
      // generate SMILES of the given flavor
      moleculeToSMILES(molecule, flavor)
    } else {
      null
    }
  }
}
