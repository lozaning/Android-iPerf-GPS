// Generated by view binder compiler. Do not edit!
package com.example.androidproject.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.androidproject.R;
import com.example.androidproject.map.SpeedVisualizationMap;
import com.google.android.material.button.MaterialButton;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final LinearLayout buttonContainer;

  @NonNull
  public final MaterialButton exportButton;

  @NonNull
  public final SpeedVisualizationMap mainMap;

  @NonNull
  public final LinearLayout mapControlsContainer;

  @NonNull
  public final MaterialButton markApButton;

  @NonNull
  public final MaterialButton recenterButton;

  @NonNull
  public final MaterialButton satelliteToggleButton;

  private ActivityMainBinding(@NonNull ConstraintLayout rootView,
      @NonNull LinearLayout buttonContainer, @NonNull MaterialButton exportButton,
      @NonNull SpeedVisualizationMap mainMap, @NonNull LinearLayout mapControlsContainer,
      @NonNull MaterialButton markApButton, @NonNull MaterialButton recenterButton,
      @NonNull MaterialButton satelliteToggleButton) {
    this.rootView = rootView;
    this.buttonContainer = buttonContainer;
    this.exportButton = exportButton;
    this.mainMap = mainMap;
    this.mapControlsContainer = mapControlsContainer;
    this.markApButton = markApButton;
    this.recenterButton = recenterButton;
    this.satelliteToggleButton = satelliteToggleButton;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.buttonContainer;
      LinearLayout buttonContainer = ViewBindings.findChildViewById(rootView, id);
      if (buttonContainer == null) {
        break missingId;
      }

      id = R.id.exportButton;
      MaterialButton exportButton = ViewBindings.findChildViewById(rootView, id);
      if (exportButton == null) {
        break missingId;
      }

      id = R.id.mainMap;
      SpeedVisualizationMap mainMap = ViewBindings.findChildViewById(rootView, id);
      if (mainMap == null) {
        break missingId;
      }

      id = R.id.mapControlsContainer;
      LinearLayout mapControlsContainer = ViewBindings.findChildViewById(rootView, id);
      if (mapControlsContainer == null) {
        break missingId;
      }

      id = R.id.markApButton;
      MaterialButton markApButton = ViewBindings.findChildViewById(rootView, id);
      if (markApButton == null) {
        break missingId;
      }

      id = R.id.recenterButton;
      MaterialButton recenterButton = ViewBindings.findChildViewById(rootView, id);
      if (recenterButton == null) {
        break missingId;
      }

      id = R.id.satelliteToggleButton;
      MaterialButton satelliteToggleButton = ViewBindings.findChildViewById(rootView, id);
      if (satelliteToggleButton == null) {
        break missingId;
      }

      return new ActivityMainBinding((ConstraintLayout) rootView, buttonContainer, exportButton,
          mainMap, mapControlsContainer, markApButton, recenterButton, satelliteToggleButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
