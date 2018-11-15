package edu.ucdavis.fiehnlab.ctsrest.client.api

import edu.ucdavis.fiehnlab.ctsrest.client.types._

trait CtsService {
  def convert(from: String, to: String, searchTerm: String): Seq[ConversionResult]
  def score(from: String, value: String, algorithm: String): ScoreResult
  def inchiKey2Mol (inchikey: String): MoleculeResponse
  def expandFormula (formula: String): FormulaResponse
  def compoundInfo (inchikey: String): CompoundResponse
  def compoundSynonyms(inchikey: String): Seq[String]
  def compoundExtidCount (inchikey: String): ExtidCountResponse
  def compoundBiologicalCount(inchikey: String): Map[String, Int]
  def chemifyQuery (name: String): String
  def sourceIdNames(): Seq[String]
  def targetIdNames(): Seq[String]
  def mol2Inchi (mol: String): InchiPairResponse
  def inchi2Mol (inchicode: String): MoleculeResponse
  def smiles2Inchi (smilesCode: String): String
  def inchiCode2InchiKey (inchiCode: String): InchiPairResponse
}
