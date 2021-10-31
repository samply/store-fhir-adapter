#!/bin/bash -e

mkdir ClinicalImpression
mkdir Condition
mkdir Encounter
mkdir MedicationStatement
mkdir Observation
mkdir Organization
mkdir Patient
mkdir Procedure
mkdir Specimen

ls -1 | grep ClinicalImpression_ | xargs -J % mv % ClinicalImpression
ls -1 | grep Condition_ | xargs -J % mv % Condition
ls -1 | grep Encounter_ | xargs -J % mv % Encounter
ls -1 | grep MedicationStatement_ | xargs -J % mv % MedicationStatement
ls -1 | grep Observation_ | xargs -J % mv % Observation
ls -1 | grep Organization_ | xargs -J % mv % Organization
ls -1 | grep Patient_ | xargs -J % mv % Patient
ls -1 | grep Procedure_ | xargs -J % mv % Procedure
ls -1 | grep Specimen_ | xargs -J % mv % Specimen

blazectl upload --server http://localhost:8090/fhir Organization
blazectl upload --server http://localhost:8090/fhir Patient
blazectl upload --server http://localhost:8090/fhir Encounter
blazectl upload --server http://localhost:8090/fhir Specimen
blazectl upload --server http://localhost:8090/fhir Condition
blazectl upload --server http://localhost:8090/fhir Observation
blazectl upload --server http://localhost:8090/fhir ClinicalImpression
blazectl upload --server http://localhost:8090/fhir Procedure
blazectl upload --server http://localhost:8090/fhir MedicationStatement
