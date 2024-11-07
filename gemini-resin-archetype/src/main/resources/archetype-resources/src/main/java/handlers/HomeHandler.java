package ${package}.handlers;

import com.khulnasoft.gemini.*;
import com.khulnasoft.gemini.path.*;
import com.khulnasoft.gemini.path.annotation.*;

public class HomeHandler extends MethodUriHandler<Context>
{

  public HomeHandler(GeminiApplication app)
  {
    super(app);
  }

  @Path
  @Get
  public boolean root() throws Exception {
    return json();
  }
}
