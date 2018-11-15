package edu.ucdavis.fiehnlab.ctsrest.client.types

import com.fasterxml.jackson.annotation.JsonProperty

case class ConversionResult(
                               fromIdentifier: String,
                               toIdentifier: String,
                               searchTerm: String,
                               results: Seq[String]
                           )

case class ScoreResult(
                          searchTerm: String,
                          from: String,
                          result: Seq[ScoredInchi]
                      )

case class ScoredInchi(
                          @JsonProperty("InChIKey")
                          inchikey: String,
                          score: Float
                      )

case class MoleculeResponse(
                               molecule: String,
                               message: String
                           )

case class FormulaResponse(
                              formula: String,
                              result: String,
                              error: String
                          )

case class CompoundResponse(
                               inchikey: String,
                               inchicode: String,
                               molweight: Double,
                               exactmass: Double,
                               formula: String,
                               synonyms: Seq[Synonym],
                               externalIds: Seq[ExtId]
                           )

case class ExtidCountResponse(
                                 datasource_count: Int
                             )

case class InchiPairResponse(
                                inchikey: String,
                                inchicode: String
                            )

case class BatchRequest(
                           from: String,
                           to: Seq[String],
                           searchTerms: Seq[String]
                       )

case class Synonym(
                      `type`: String,
                      name: String,
                      score: Int
                  )

case class HitResponse(
                          result: String,
                          query: String,
                          algorithm: String,
                          score: Double,
                          scoring_algorithm: String,
                          enhancements: Map[String, String]
                      )

case class ExtId(
                    name: String,
                    value: String,
                    url: String
                )
