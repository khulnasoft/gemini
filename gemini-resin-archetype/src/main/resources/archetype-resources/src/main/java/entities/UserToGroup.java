package ${package}.entities;

import com.khulnasoft.data.*;
import com.khulnasoft.data.annotation.*;

/**
 * Represents a user group relation for the application.
 */
@Relation
public class UserToGroup
  implements EntityRelationDescriptor<User, Group>
{
    @Left
    User userId;

    @Right
    Group groupId;
}
