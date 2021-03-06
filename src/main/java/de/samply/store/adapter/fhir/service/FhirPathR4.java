package de.samply.store.adapter.fhir.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.fhirpath.FhirPathExecutionException;
import ca.uhn.fhir.fhirpath.IFhirPath;
import java.util.List;
import java.util.Optional;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.hapi.ctx.HapiWorkerContext;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.utils.FHIRPathEngine;
import org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext;

/**
 * A FHIRPath engine.
 */
public class FhirPathR4 implements IFhirPath {

  private final FHIRPathEngine myEngine;

  /**
   * Creates a new FHIRPath engine.
   *
   * @param context           the FHIR context
   * @param evaluationContext the evaluation context
   */
  public FhirPathR4(FhirContext context, IEvaluationContext evaluationContext) {
    IValidationSupport validationSupport = context.getValidationSupport();
    myEngine = new FHIRPathEngine(new HapiWorkerContext(context, validationSupport));
    myEngine.setHostServices(evaluationContext);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IBase> List<T> evaluate(IBase theInput, String thePath,
      Class<T> theReturnType) {
    List<Base> result;
    try {
      result = myEngine.evaluate((Base) theInput, thePath);
    } catch (FHIRException e) {
      throw new FhirPathExecutionException(e);
    }

    for (Base next : result) {
      if (!theReturnType.isAssignableFrom(next.getClass())) {
        throw new FhirPathExecutionException(
            "FluentPath expression \"" + thePath + "\" returned unexpected type " + next.getClass()
                .getSimpleName() + " - Expected " + theReturnType.getName());
      }
    }

    return (List<T>) result;
  }

  @Override
  public <T extends IBase> Optional<T> evaluateFirst(IBase theInput, String thePath,
      Class<T> theReturnType) {
    return evaluate(theInput, thePath, theReturnType).stream().findFirst();
  }

  @Override
  public void parse(String theExpression) {
    myEngine.parse(theExpression);
  }


}
