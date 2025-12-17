package com.example.individualassign_noranis;

public class BillCalculator {

    // --- TARIFF RATES BASED ON THE PROVIDED TABLE ---
    private static final double RATE_BLOCK1 = 0.218; // 0 - 200 kWh
    private static final double RATE_BLOCK2 = 0.334; // 201 - 300 kWh
    private static final double RATE_BLOCK3 = 0.516; // 301 - 600 kWh
    private static final double RATE_BLOCK4 = 0.546; // 601 - 900 kWh
    private static final double RATE_BLOCK5 = 0.571; // > 900 kWh (Assuming standard rate for completeness)


    /**
     * Calculates the electricity bill based on tiered tariff blocks.
     * @param unitUsed The total units (kWh) consumed.
     * @param rebatePercent The rebate percentage (e.g., 5.0 for 5%).
     * @return A double array: [Total Charges, Final Cost]
     */
    public static double[] calculate(double unitUsed, double rebatePercent) {double totalCharges = 0;
        double remainingUnits = unitUsed;

        // Block 1: First 200 kWh
        double block1Units = Math.min(remainingUnits, 200);
        totalCharges += block1Units * RATE_BLOCK1;
        remainingUnits -= block1Units;

        // Block 2: Next 100 kWh (201 - 300)
        if (remainingUnits > 0) {
            double block2Units = Math.min(remainingUnits, 100);
            totalCharges += block2Units * RATE_BLOCK2;
            remainingUnits -= block2Units;
        }

        // Block 3: Next 300 kWh (301 - 600)
        if (remainingUnits > 0) {
            double block3Units = Math.min(remainingUnits, 300);
            totalCharges += block3Units * RATE_BLOCK3;
            remainingUnits -= block3Units;
        }

        // Block 4: Next 300 kWh (601 - 900)
        if (remainingUnits > 0) {
            double block4Units = Math.min(remainingUnits, 300);
            totalCharges += block4Units * RATE_BLOCK4;
            remainingUnits -= block4Units;
        }

        // Block 5: Balance (> 900 kWh)
        if (remainingUnits > 0) {
            totalCharges += remainingUnits * RATE_BLOCK5;
        }

        // --- Apply Rebate ---
        double rebateFactor = rebatePercent / 100.0;
        double finalCost = totalCharges - (totalCharges * rebateFactor);

        // Return results [Total Charges, Final Cost]
        return new double[]{totalCharges, finalCost};
    }
}