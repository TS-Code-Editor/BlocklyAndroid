/*
 * This file is part of Android AppStudio [https://github.com/Innovative-CST/AndroidAppStudio].
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

package com.icst.android.appstudio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;

// Manages extension-related operations
public class ExtensionGenerator {

  public static void main(String[] args) throws Exception {
    File outputDir = new File(args[0]);
    boolean installExtensions = Boolean.parseBoolean(args[1]);
    boolean isDeveloperMode = Boolean.parseBoolean(args[2]);
    File storage = new File(args[3]);

    // Handle extensions processing and installation
    processExtensions(outputDir);
    installExtensions(installExtensions, isDeveloperMode, storage, outputDir);
  }

  // Handles serialization and task extraction
  private static void processExtensions(File outputDir) throws Exception {
    ArrayList<HashMap<String, Object>> extensions = ExtensionsManager.getExtensions();

    for (HashMap<String, Object> extension : extensions) {
      if (extension.containsKey(ExtensionsManager.EXTENSION_BUNDLE)) {
        File extensionFile =
            new File(outputDir, (String) extension.get(ExtensionsManager.EXTENSION_FILE_NAME));
        String taskName =
            extractTaskName((String) extension.get(ExtensionsManager.EXTENSION_FILE_NAME));
        serialize(extension.get(ExtensionsManager.EXTENSION_BUNDLE), extensionFile, taskName);
      } else {
        throw new Exception(ExtensionsManager.EXTENSION_BUNDLE.concat(" key is not set."));
      }
    }
  }

  // Handles the installation of extensions if required
  private static void installExtensions(
      boolean installExtensions, boolean isDeveloperMode, File storage, File outputDir) {
    if (!installExtensions || !isDeveloperMode || storage.getAbsolutePath().equals("NOT_PROVIDED"))
      return;

    File ideDirectory = new File(storage, ".AndroidAppBuilder");
    File extensionDir = new File(ideDirectory, "Extension");

    createDirectories(outputDir, extensionDir);

    installFiles(outputDir, extensionDir);

    System.out.println(
        "\nAndroid AppStudio will search the installed extensions if it is built using the current local.properties configuration.");
  }

  // Creates required directories if they don't exist
  private static void createDirectories(File outputDir, File extensionDir) {
    if (!outputDir.exists()) outputDir.mkdirs();
    if (!extensionDir.exists()) extensionDir.mkdirs();
  }

  // Copies extension files to the designated directory
  private static void installFiles(File outputDir, File extensionDir) {
    System.out.println("> Task :extension:installExtension\n");

    for (File file : outputDir.listFiles()) {
      Path source = Path.of(file.toURI());
      Path destination = Path.of(new File(extensionDir, file.getName()).toURI());

      if (file.isDirectory()) continue;

      try {
        System.out.println("Installing ".concat(file.getName()).concat(" in your file system."));
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  // Serializes the extension bundle
  private static void serialize(Object object, File path, String taskName) throws Exception {
    try (FileOutputStream fileOutputStream = new FileOutputStream(path);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

      objectOutputStream.writeObject(object);
      System.out.println("> Task :extension:".concat(taskName));
    } catch (Exception e) {
      System.out.println("Failed to serialize ".concat(path.getAbsolutePath()));
      throw e;
    }
  }

  // Extracts the task name based on the file name
  private static String extractTaskName(String fileName) {
    String baseName = fileName.replace(".extaas", "");
    String[] words = baseName.split("(?=[A-Z])|_");

    StringBuilder taskName = new StringBuilder("generate");
    for (String word : words) {
      if (!word.isEmpty()) {
        taskName.append(Character.toUpperCase(word.charAt(0)));
        if (word.length() > 1) {
          taskName.append(word.substring(1).toLowerCase());
        }
      }
    }
    return taskName.toString();
  }
}