library Query
using FHIR version '4.0.0'
include FHIRHelpers version '4.0.0'

context Patient

define InInitialPopulation:
  exists([Condition] C where exists(C.code.coding.where(system='http://fhir.de/CodeSystem/dimdi/icd-10-gm' and code[0] in {'C', 'D'})))
