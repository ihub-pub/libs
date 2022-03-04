package test;

import pub.ihub.core.AutoConfigPostProcessor;
import pub.ihub.core.BaseConfigEnvironmentPostProcessor;

@AutoConfigPostProcessor
public final class OtherConfigPostProcessor extends BaseConfigEnvironmentPostProcessor {
  @Override
  protected String getActiveProfile() {
    return "other";
  }
}
