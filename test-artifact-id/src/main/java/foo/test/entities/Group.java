package foo.test.entities;

import com.khulnasoft.data.annotation.*;
import com.khulnasoft.gemini.pyxis.*;
import com.khulnasoft.util.*;

/**
 * Represents a user group for the application.
 */
@CachedEntity
public class Group
      extends BasicUserGroup
      implements PersistenceAware
{

    //
    // Constructor.
    //
    public Group() {
        super();
    }

}
