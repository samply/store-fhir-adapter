package de.samply.store.adapter.fhir.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.TypeDetails;
import org.hl7.fhir.r4.model.ValueSet;
import org.hl7.fhir.r4.utils.FHIRPathEngine.IEvaluationContext;

public class MyIEvaluationContext implements IEvaluationContext {

  private final Map<String, Resource> resources;

  public MyIEvaluationContext(){
    this(Map.of());
  }

  public MyIEvaluationContext(Map<String, Resource> resources) {
    Objects.requireNonNull(resources);
    this.resources = resources;
  }

  @Override
  public Base resolveConstant(Object appContext, String name, boolean beforeContext)
      throws PathEngineException {
    return null;
  }

  @Override
  public TypeDetails resolveConstantType(Object appContext, String name)
      throws PathEngineException {
    return null;
  }

  @Override
  public boolean log(String argument, List<Base> focus) {
    return false;
  }

  @Override
  public FunctionDetails resolveFunction(String functionName) {
    return null;
  }

  @Override
  public TypeDetails checkFunction(Object appContext, String functionName,
      List<TypeDetails> parameters) throws PathEngineException {
    return null;
  }

  @Override
  public List<Base> executeFunction(Object appContext, List<Base> focus, String functionName,
      List<List<Base>> parameters) {
    return null;
  }

  @Override
  public Base resolveReference(Object appContext, String url) throws FHIRException {
    return this.resources.getOrDefault(url, null);
  }

  @Override
  public boolean conformsToProfile(Object appContext, Base item, String url)
      throws FHIRException {
    return false;
  }

  @Override
  public ValueSet resolveValueSet(Object appContext, String url) {
    return null;
  }
}
