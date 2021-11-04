package de.samply.store.adapter.fhir.util;

import de.samply.store.adapter.fhir.util.Anomaly.Fault;
import de.samply.store.adapter.fhir.util.Anomaly.NotFound;

/**
 * Anomalies represent errors in a simple, actionable and generic way.
 *
 * @see <a href="https://github.com/cognitect-labs/anomalies">cognitect.anomalies</a>
 */
public sealed interface Anomaly permits NotFound, Fault {

  /**
   * Returns the message of this anomaly.
   *
   * @return the message
   */
  String msg();

  /**
   * Something was not found, like 404 in HTTP.
   */
  record NotFound(String msg) implements Anomaly {

  }

  /**
   * Some general callee bug, like 500 in HTTP.
   */
  record Fault(String msg) implements Anomaly {

  }
}
