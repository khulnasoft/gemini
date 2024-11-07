package com.khulnasoft.gemini.path;

import com.khulnasoft.gemini.Context;

public class NotImplementedHandler<C extends Context>
  implements PathHandler<C>
{
  @Override
  public boolean prehandle(PathSegments segments, C context) {
    //Do Nothing
    return false;
  }

  @Override
  public boolean handle(PathSegments segments, C context)
  {
    context.setStatus(501);
    return true;
  }

  @Override
  public void posthandle(PathSegments segments, C context)
  {
    // Do nothing
  }
}
