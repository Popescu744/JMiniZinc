package at.siemens.ct.jmz;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.siemens.ct.jmz.elements.NamedElement;
import at.siemens.ct.jmz.elements.constraints.Constraint;

/**
 * Maintains names and group memberships of {@link Constraint}s.
 * 
 * @author z003ft4a (Richard Taupe)
 *
 */
public class ConstraintRegistry {

  public static final String NAME_SEPARATOR = ".";

  private final Map<String, Set<String>> mapGroupToNames = new HashMap<>();
  private final Map<List<String>, Constraint> mapGroupAndNameToConstraint = new HashMap<>();
  private final Set<String> ignoredGroups = new HashSet<>();
  private final Set<List<String>> ignoredKeys = new HashSet<>();

  /**
   * Registers a constraint.
   * @param constraint
   */
  public void register(Constraint constraint) {
    String group = constraint.getConstraintGroup();
    String name = constraint.getConstraintName();
    mapGroupToName(group, name);
    mapGroupAndNameToConstraint(group, name, constraint);
  }

  private void mapGroupToName(String group, String name) {
    if (!mapGroupToNames.containsKey(group)) {
      mapGroupToNames.put(group, new HashSet<>());
    }
    mapGroupToNames.get(group).add(name);
  }

  private void mapGroupAndNameToConstraint(String group, String name, Constraint constraint) {
    List<String> key = key(group, name);
    if (mapGroupAndNameToConstraint.containsKey(key))
      throw new IllegalArgumentException(
          "Constraint already exists: " + group + NAME_SEPARATOR + name);
    
    mapGroupAndNameToConstraint.put(key, constraint);
  }

  public void ignoreGroup(String group) {
    ignoredGroups.add(group);
  }

  public void ignoreConstraint(String group, String name) {
    ignoredKeys.add(key(group, name));
  }

  public boolean isIgnored(Constraint constraint) {
    String group = constraint.getConstraintGroup();
    String name = constraint.getConstraintName();

    return ignoredGroups.contains(group) || ignoredKeys.contains(key(group, name));
  }

  private List<String> key(String group, String name) {
    return Arrays.asList(group, name);
  }

  /**
   * Builds a constraint name from a given prefix and list of {@link NamedElement}s.
   * 
   * @param prefix
   * @param namedElements
   * @return the concatenation of the prefix and the names of the given elements, separated by {@link #NAME_SEPARATOR}.
   */
  public static String buildName(String prefix, NamedElement... namedElements) {
    StringBuilder builder = new StringBuilder(prefix);
    for (NamedElement namedElement : namedElements) {
      builder.append(NAME_SEPARATOR);
      builder.append(namedElement.getName());
    }
    return builder.toString();
  }

}