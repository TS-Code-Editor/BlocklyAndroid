/*
 * This file is part of Android AppStudio [https://github.com/TS-Code-Editor/AndroidAppStudio].
 *
 * License Agreement
 * This software is licensed under the terms and conditions outlined below. By accessing, copying, modifying, or using this software in any way, you agree to abide by these terms.
 *
 * 1. **  Copy and Modification Restrictions  **
 *    - You are not permitted to copy or modify the source code of this software without the permission of the owner, which may be granted publicly on GitHub Discussions or on Discord.
 *    - If permission is granted by the owner, you may copy the software under the terms specified in this license agreement.
 *    - You are not allowed to permit others to copy the source code that you were allowed to copy by the owner.
 *    - Modified or copied code must not be further copied.
 * 2. **  Contributor Attribution  **
 *    - You must attribute the contributors by creating a visible list within the application, showing who originally wrote the source code.
 *    - If you copy or modify this software under owner permission, you must provide links to the profiles of all contributors who contributed to this software.
 * 3. **  Modification Documentation  **
 *    - All modifications made to the software must be documented and listed.
 *    - the owner may incorporate the modifications made by you to enhance this software.
 * 4. **  Consistent Licensing  **
 *    - All copied or modified files must contain the same license text at the top of the files.
 * 5. **  Permission Reversal  **
 *    - If you are granted permission by the owner to copy this software, it can be revoked by the owner at any time. You will be notified at least one week in advance of any such reversal.
 *    - In case of Permission Reversal, if you fail to acknowledge the notification sent by us, it will not be our responsibility.
 * 6. **  License Updates  **
 *    - The license may be updated at any time. Users are required to accept and comply with any changes to the license.
 *    - In such circumstances, you will be given 7 days to ensure that your software complies with the updated license.
 *    - We will not notify you about license changes; you need to monitor the GitHub repository yourself (You can enable notifications or watch the repository to stay informed about such changes).
 * By using this software, you acknowledge and agree to the terms and conditions outlined in this license agreement. If you do not agree with these terms, you are not permitted to use, copy, modify, or distribute this software.
 *
 * Copyright © 2024 Dev Kumar
 */

package com.tscodeeditor.android.appstudio.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.tscodeeditor.android.appstudio.MyApplication;
import com.tscodeeditor.android.appstudio.R;
import com.tscodeeditor.android.appstudio.adapters.ProjectListAdapter;
import com.tscodeeditor.android.appstudio.databinding.ActivityProjectManagerBinding;
import com.tscodeeditor.android.appstudio.models.ProjectModel;
import com.tscodeeditor.android.appstudio.utils.EnvironmentUtils;
import com.tscodeeditor.android.appstudio.utils.PermissionUtils;
import com.tscodeeditor.android.appstudio.utils.serialization.ProjectModelSerializationUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class ProjectManagerActivity extends BaseActivity {
  private ActivityProjectManagerBinding binding;

  // Contants for showing the section easily
  public static final int LOADING_SECTION = 0;
  public static final int NO_PROJECTS_YET_SECTION = 1;
  public static final int PROJECT_LIST_SECTION = 2;
  public static final int ERROR_SECTION = 3;

  // Result launcher
  public ActivityResultLauncher<Intent> projectListUpdateActivityResultLauncher;

  @Override
  protected void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    // Initializing
    binding = ActivityProjectManagerBinding.inflate(getLayoutInflater());
    projectListUpdateActivityResultLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
              @Override
              public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                  tryToLoadProjects();
                }
              }
            });

    // Set layout of activity
    setContentView(binding.getRoot());

    // SetUp the toolbar
    binding.toolbar.setTitle(R.string.app_name);
    setSupportActionBar(binding.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    ActionBarDrawerToggle toggle =
        new ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar, R.string.app_name, R.string.app_name);
    binding.toolbar.setNavigationOnClickListener(
        v -> {
          if (binding.drawerLayout.isDrawerVisible(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START);
          } else {
            binding.drawerLayout.openDrawer(GravityCompat.START);
          }
        });
    binding.drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    // SetUp the items click events in drawer
    binding.navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          if (menuItem.getItemId() == R.id.source_License) {
            Intent licenseActivity = new Intent();
            licenseActivity.setClass(this, LicenseActivity.class);
            startActivity(licenseActivity);
          }
          if (menuItem.getItemId() == R.id.about_app) {
            Intent appInfo = new Intent();
            appInfo.setClass(this, AboutAppActivity.class);
            startActivity(appInfo);
          }
          if (menuItem.getItemId() == R.id.instagram) {
            Intent instagram = new Intent();
            instagram.setAction(Intent.ACTION_VIEW);
            instagram.setData(Uri.parse(MyApplication.INSTAGRAM));
            startActivity(instagram);
          }
          if (menuItem.getItemId() == R.id.youtube) {
            Intent youtube = new Intent();
            youtube.setAction(Intent.ACTION_VIEW);
            youtube.setData(Uri.parse(MyApplication.YOUTUBE));
            startActivity(youtube);
          }
          if (menuItem.getItemId() == R.id.discord) {
            Intent discord = new Intent();
            discord.setAction(Intent.ACTION_VIEW);
            discord.setData(Uri.parse(MyApplication.DISCORD));
            startActivity(discord);
          }
          if (menuItem.getItemId() == R.id.about_team) {
            Intent aboutTeam = new Intent();
            aboutTeam.setClass(this, AboutTeamActivity.class);
            startActivity(aboutTeam);
          }
          return true;
        });
    /*
     * Initialize new project click listener.
     */
    binding.fab.setOnClickListener(
        v -> {
          createProject();
        });
    binding.createNewProject.setOnClickListener(
        v -> {
          createProject();
        });
    /*
     * Ask for storage permission if not granted.
     * Load projects if storage permission is granted.
     * Show storage permission denied error when storage permission is denied and ask to grant storage permission.
     */
    if (PermissionUtils.isStoagePermissionGranted(this)) {
      tryToLoadProjects();
    } else {
      showError(getString(R.string.storage_permission_denied));
      PermissionUtils.showStoragePermissionDialog(this);
    }
  }

  /*
   * Method for switching the section quickly.
   * All other section will be GONE except the section of which the section code is provided
   */
  public void switchSection(int section) {
    binding.loading.setVisibility(section == LOADING_SECTION ? View.VISIBLE : View.GONE);
    binding.noProjectsYet.setVisibility(
        section == NO_PROJECTS_YET_SECTION ? View.VISIBLE : View.GONE);
    binding.projectList.setVisibility(section == PROJECT_LIST_SECTION ? View.VISIBLE : View.GONE);
    binding.errorSection.setVisibility(section == ERROR_SECTION ? View.VISIBLE : View.GONE);
  }

  public void showError(String errorText) {
    switchSection(ERROR_SECTION);
    binding.errorText.setText(errorText);
  }

  /*
   * Method for creating new project.
   * Only proceed for creating project when storage permission is granted.
   * Ask for storage permission when not granted.
   */
  public void createProject() {
    if (!PermissionUtils.isStoagePermissionGranted(this)) {
      /*
       * Storage permission is not granted.
       * Requesting for storage permission.
       * Aborting new project task.
       */
      PermissionUtils.showStoragePermissionDialog(this);
      return;
    }
    /*
     * Go to ProjectModelConfigrationActivity to create new project.
     */

    Intent createNewProject = new Intent();
    createNewProject.setClass(this, ProjectModelConfigrationActivity.class);
    createNewProject.putExtra("isNewProject", true);
    projectListUpdateActivityResultLauncher.launch(createNewProject);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  public void tryToLoadProjects() {
    /*
     * Show LOADING_SECTION.
     * Then load project in a saparate thread
     */
    switchSection(LOADING_SECTION);
    Executors.newSingleThreadExecutor()
        .execute(
            () -> {
              /*
               * Check if the PROJECTS directory exists or not.
               * If not exists then:
               * - Create PROJECTS directory now.
               * - Show NO_PROJECTS_YET_SECTION.
               * - Stop the thread on further execution.
               */
              if (!EnvironmentUtils.PROJECTS.exists()) {
                EnvironmentUtils.PROJECTS.mkdirs();
                runOnUiThread(
                    () -> {
                      switchSection(NO_PROJECTS_YET_SECTION);
                    });
                return;
              }
              /*
               * List all the file list available in PROJECTS.
               * And check all the paths and add ProjectModel to projectModelList when confirmed that it is a project directory.
               */
              ArrayList<ProjectModel> projectModelList = new ArrayList<ProjectModel>();
              ArrayList<File> projectModelFileList = new ArrayList<File>();
              for (File file : EnvironmentUtils.PROJECTS.listFiles()) {
                /*
                 * Only check further if the file is a directory.
                 * If file is a file then skip it.
                 */
                if (file.isFile()) continue;
                /*
                 * Check if the ProjectModel file exists in the directory(file).
                 * If it does not exists then skip it.
                 */
                if (!new File(file, EnvironmentUtils.PROJECT_CONFIGRATION).exists()) continue;
                /*
                 * Logic to Deserialize the path as it exists as file.
                 * Add ProjectModel to list once Deserialized
                 */
                ProjectModelSerializationUtils.deserialize(
                    new File(file, EnvironmentUtils.PROJECT_CONFIGRATION),
                    new ProjectModelSerializationUtils.DeserializerListener() {

                      @Override
                      public void onSuccessfullyDeserialized(ProjectModel mProjectModel) {
                        /*
                         * Add the ProjectModel to ArrayList as it is project
                         */
                        projectModelList.add(mProjectModel);
                        projectModelFileList.add(file);
                      }

                      @Override
                      public void onFailed(int errorCode, Exception e) {}
                    });
              }
              /*
               * Updating the UI according to the loaded project.
               * - Show ProjectList if the number of ProjectModel in projectModelList is atleast 1.
               * - Show no projects yet section if there is no ProjectModel in projectModelList.
               */
              runOnUiThread(
                  () -> {
                    switchSection(
                        projectModelList.size() > 0
                            ? PROJECT_LIST_SECTION
                            : NO_PROJECTS_YET_SECTION);
                    binding.list.setAdapter(
                        new ProjectListAdapter(
                            projectModelList, projectModelFileList, ProjectManagerActivity.this));
                    binding.list.setLayoutManager(
                        new LinearLayoutManager(ProjectManagerActivity.this));
                  });
            });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (PermissionUtils.isStoagePermissionGranted(this)) {
      tryToLoadProjects();
    } else {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        PermissionUtils.showRationaleOfStoragePermissionDialog(this);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int arg0, String[] arg1, int[] arg2) {
    super.onRequestPermissionsResult(arg0, arg1, arg2);
    switch (arg0) {
      case 1:
      case -1:
      case 10:
        boolean isDenied = false;
        for (int position = 0; position < arg2.length; position++) {
          if (arg2[position] == PackageManager.PERMISSION_DENIED) {
            isDenied = true;
            if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(arg1[position])) {
              if (Build.VERSION.SDK_INT >= 23) {
                if (shouldShowRequestPermissionRationale(arg1[position])) {
                  PermissionUtils.showRationaleOfStoragePermissionDialog(this);
                } else {
                  PermissionUtils.showStoragePermissionDialogForGoToSettings(this);
                }
              }
            }
          }
        }
        if (!isDenied) {
          tryToLoadProjects();
        }
        break;
    }
  }
}
