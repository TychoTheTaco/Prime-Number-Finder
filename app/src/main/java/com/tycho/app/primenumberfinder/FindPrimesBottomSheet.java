package com.tycho.app.primenumberfinder;

import android.app.Dialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.View;

/**
 * @author Tycho Bellers
 *         Date Created: 3/14/2017
 */

public class FindPrimesBottomSheet extends BottomSheetDialogFragment{

    @Override
    public void setupDialog(Dialog dialog, int style){
        super.setupDialog(dialog, style);
        final View rootView = View.inflate(getContext(), R.layout.bottom_sheet_primes, null);
        dialog.setContentView(rootView);
    }
}
