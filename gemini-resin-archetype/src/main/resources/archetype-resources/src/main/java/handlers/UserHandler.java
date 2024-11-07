package ${package}.handlers;

import com.khulnasoft.gemini.*;
import com.khulnasoft.gemini.path.annotation.*;
import com.khulnasoft.gemini.pyxis.handler.*;
import ${package}.entities.*;

public class UserHandler extends SecureMethodUriHandler<Context, User>
{

  public UserHandler(GeminiApplication app)
  {
    super(app);
  }

  @Path
  @Get
  public boolean home()
  {
    return mustache("user");
  }

}
