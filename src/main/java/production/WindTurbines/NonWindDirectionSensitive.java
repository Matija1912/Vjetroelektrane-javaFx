package production.WindTurbines;

public sealed interface NonWindDirectionSensitive permits StaticHorizontalAxisTurbine {
    Double windOrientationLossCoefficient(Double windOrientationInDegrees);
}
