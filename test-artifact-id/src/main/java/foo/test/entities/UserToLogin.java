package foo.test.entities;

import com.khulnasoft.data.*;
import com.khulnasoft.data.annotation.*;
import com.khulnasoft.gemini.pyxis.*;

@Relation
public class UserToLogin
  implements EntityRelationDescriptor<User, Login>
{
  @Left
  User userId;
  
  @Right
  Login loginId;
}
