/*
 *  This file is part of Android AppStudio.
 *
 *  Android AppStudio is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Android AppStudio is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *   along with Android AppStudio.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.tscodeeditor.android.appstudio.fragments.projectmodelconfig;

import androidx.fragment.app.Fragment;
import com.tscodeeditor.android.appstudio.models.ProjectModel;

public class ProjectModelConfigBaseFragment extends Fragment {
  private boolean isNewProject;
  private ProjectModel mProjectModel;

  public ProjectModelConfigBaseFragment(boolean isNewProject, ProjectModel mProjectModel) {
    this.isNewProject = isNewProject;
    this.mProjectModel = mProjectModel;
  }

  public boolean getIsRequiredFieldsProperlyFilled() {
    return false;
  }

  public boolean getIsNewProject() {
    return this.isNewProject;
  }

  public void setIsNewProject(boolean isNewProject) {
    this.isNewProject = isNewProject;
  }

  public ProjectModel getMProjectModel() {
    return this.mProjectModel;
  }

  public void setMProjectModel(ProjectModel mProjectModel) {
    this.mProjectModel = mProjectModel;
  }

  /*
   * Method to update the mProjectModel with data in current fragment.
   */
  public void addValueInProjectModelOfFragment() {}

  /*
   * Method to set the mProjectModel data into fields of current fragment.
   */
  public void setProjectModelValueIntoFields() {}
}