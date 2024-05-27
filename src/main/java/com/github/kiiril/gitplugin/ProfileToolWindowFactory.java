package com.github.kiiril.gitplugin;

import com.github.kiiril.gitplugin.components.CommitsTable;
import com.github.kiiril.gitplugin.components.ExternalLibrariesList;
import com.github.kiiril.gitplugin.components.LanguagePercentagePanel;
import com.github.kiiril.gitplugin.services.ProfileService;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.panels.VerticalBox;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBFont;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ProfileToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ProfileToolWindowContent toolWindowContent = new ProfileToolWindowContent(project);
        Content content = ContentFactory.getInstance().createContent(toolWindowContent.getContentPanel(),"", false);
        toolWindow.getContentManager().addContent(content);
    }

    private static class ProfileToolWindowContent {
        private final JPanel contentPanel = new JBPanel<>();

        public ProfileToolWindowContent(Project project) {
            ProfileService profileService = project.getService(ProfileService.class);
            contentPanel.setLayout(new BorderLayout());
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add margins
            try {
                VerticalBox northPanel = new VerticalBox();

                JPanel userData = new JPanel(); // Adjust horizontal gap as needed
                userData.setLayout(new BoxLayout(userData, BoxLayout.X_AXIS));
                String[] userAndEmail = profileService.getUserAndEmailJava().get();
                JBLabel userLabel = new JBLabel(userAndEmail[0]);
                JBFont font = JBFont.h0();
                font.asBold();
                Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
                attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
                userLabel.setFont(font.deriveFont(attributes));
                JBLabel emailLabel = new JBLabel(userAndEmail[1]);
                emailLabel.setFont(JBFont.small());
                userData.add(userLabel);
                userData.add(Box.createRigidArea(new Dimension(5, 0)));
                userData.add(emailLabel);

                JPanel horizontalBox = new JPanel();
                horizontalBox.setLayout(new BoxLayout(horizontalBox, BoxLayout.X_AXIS));
                horizontalBox.add(new JBLabel("Commits by user: " + profileService.getNumberOfCommitsByUserJava().get()));
                horizontalBox.add(Box.createRigidArea(new Dimension(10, 0)));
                horizontalBox.add(new ExternalLibrariesList(profileService.getProjectLibrariesJava().get()));

                northPanel.add(userData);
                northPanel.add(horizontalBox);

                contentPanel.add(northPanel, BorderLayout.NORTH);

                contentPanel.add(new CommitsTable(profileService.getCommitsForTableJava().get(), userAndEmail[0]), BorderLayout.CENTER);

                contentPanel.add(new LanguagePercentagePanel(profileService.getTechnologiesJava().get()), BorderLayout.SOUTH);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        public JPanel getContentPanel() {
            return contentPanel;
        }
    }
}
