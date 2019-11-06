package frLayouting2;

import java.util.EnumSet;
import org.eclipse.elk.core.data.ILayoutMetaDataProvider;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.properties.Property;

@SuppressWarnings("all")
public class FrLayouting2MetadataProvider implements ILayoutMetaDataProvider {
  /**
   * Default value for {@link #REVERSE_INPUT}.
   */
  private static final boolean REVERSE_INPUT_DEFAULT = false;
  
  /**
   * True if nodes should be placed in reverse order of their
   * appearance in the graph.
   */
  public static final IProperty<Boolean> REVERSE_INPUT = new Property<Boolean>(
            "frLayouting2.reverseInput",
            REVERSE_INPUT_DEFAULT,
            null,
            null);
  
  /**
   * Default value for {@link #ITERATIONS}.
   */
  private static final int ITERATIONS_DEFAULT = 50;
  
  /**
   * How many iterations should the alg. use.
   */
  public static final IProperty<Integer> ITERATIONS = new Property<Integer>(
            "frLayouting2.iterations",
            ITERATIONS_DEFAULT,
            null,
            null);
  
  public void apply(final org.eclipse.elk.core.data.ILayoutMetaDataProvider.Registry registry) {
    registry.register(new LayoutOptionData.Builder()
        .id("frLayouting2.reverseInput")
        .group("")
        .name("Reverse Input")
        .description("True if nodes should be placed in reverse order of their appearance in the graph.")
        .defaultValue(REVERSE_INPUT_DEFAULT)
        .type(LayoutOptionData.Type.BOOLEAN)
        .optionClass(Boolean.class)
        .targets(EnumSet.of(LayoutOptionData.Target.PARENTS))
        .visibility(LayoutOptionData.Visibility.VISIBLE)
        .create()
    );
    registry.register(new LayoutOptionData.Builder()
        .id("frLayouting2.iterations")
        .group("")
        .name("Iterations")
        .description("How many iterations should the alg. use.")
        .defaultValue(ITERATIONS_DEFAULT)
        .type(LayoutOptionData.Type.INT)
        .optionClass(Integer.class)
        .targets(EnumSet.of(LayoutOptionData.Target.PARENTS))
        .visibility(LayoutOptionData.Visibility.VISIBLE)
        .create()
    );
    new frLayouting2.options.FrLayouting2Options().apply(registry);
  }
}
