package ${package}.handlers;

import com.khulnasoft.gemini.*;
import com.khulnasoft.gemini.path.annotation.*;
import com.khulnasoft.gemini.pyxis.authorization.*;
import com.khulnasoft.gemini.pyxis.handler.*;
import ${package}.entities.*;

public class AdminHandler extends SecureMethodUriHandler<Context, User>
{

  public AdminHandler(GeminiApplication app)
  {
    super(app, new AuthorizerByAdmin(), app.getSecurity().getForceLoginRejector());
  }

  @Path
  @Get
  public boolean home()
  {
    return mustache("admin");
  }

}
