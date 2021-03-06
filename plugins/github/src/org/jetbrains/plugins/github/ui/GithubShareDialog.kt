package org.jetbrains.plugins.github.ui

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.intellij.util.ui.dialog.DialogUtils
import org.jetbrains.annotations.TestOnly
import org.jetbrains.plugins.github.authentication.accounts.GithubAccount
import org.jetbrains.plugins.github.authentication.ui.GithubAccountCombobox
import org.jetbrains.plugins.github.ui.util.DialogValidationUtils.RecordUniqueValidator
import org.jetbrains.plugins.github.ui.util.DialogValidationUtils.chain
import org.jetbrains.plugins.github.ui.util.DialogValidationUtils.notBlank
import org.jetbrains.plugins.github.ui.util.Validator
import java.awt.Component
import java.util.regex.Pattern
import javax.swing.JTextArea


class GithubShareDialog(project: Project,
                        accounts: Set<GithubAccount>,
                        defaultAccount: GithubAccount?,
                        existingRemotes: Set<String>,
                        private val accountInformationSupplier: (GithubAccount, Component) -> Pair<Boolean, Set<String>>)
  : DialogWrapper(project) {

  private val GITHUB_REPO_PATTERN = Pattern.compile("[a-zA-Z0-9_.-]+")

  private val repositoryTextField = JBTextField(project.name)
  private val privateCheckBox = JBCheckBox("Private", false)
  private val remoteTextField = JBTextField(if (existingRemotes.isEmpty()) "origin" else "github")
  private val descriptionTextArea = JTextArea()
  private val accountSelector = GithubAccountCombobox(accounts, defaultAccount) { switchAccount(it) }
  private val existingRepoValidator = RecordUniqueValidator(repositoryTextField, "Repository with selected name already exists")
  private val existingRemoteValidator = RecordUniqueValidator(remoteTextField, "Remote with selected name already exists")
    .apply { records = existingRemotes }
  private var accountInformationLoadingError: ValidationInfo? = null

  init {
    title = "Share Project On GitHub"
    setOKButtonText("Share")
    init()
    DialogUtils.invokeLaterAfterDialogShown(this) { switchAccount(accountSelector.selectedItem as GithubAccount) }
  }

  private fun switchAccount(account: GithubAccount) {
    try {
      accountInformationLoadingError = null
      accountInformationSupplier(account, window).let {
        privateCheckBox.isEnabled = it.first
        if (!it.first) privateCheckBox.toolTipText = "Your account doesn't support private repositories"
        else privateCheckBox.toolTipText = null
        existingRepoValidator.records = it.second
      }
    }
    catch (e: Exception) {
      accountInformationLoadingError = if (e is ProcessCanceledException) {
        ValidationInfo("Cannot load information for $account:\nProcess cancelled")
      }
      else ValidationInfo("Cannot load information for $account:\n$e")
      privateCheckBox.isEnabled = false
      privateCheckBox.toolTipText = null
      existingRepoValidator.records = emptySet()
      startTrackingValidation()
    }
  }

  override fun createCenterPanel() = panel {
    row("Repository name:") {
      cell {
        repositoryTextField(growX, pushX)
        privateCheckBox()
      }
    }
    row("Remote:") {
      remoteTextField(growX, pushX)
    }
    row("Description:") {
      scrollPane(descriptionTextArea)
    }
    if (accountSelector.isEnabled) {
      row("Share by:") {
        accountSelector(growX, pushX)
      }
    }
  }

  override fun doValidateAll(): List<ValidationInfo> {
    val repositoryNamePatternMatchValidator: Validator = {
      if (!GITHUB_REPO_PATTERN.matcher(repositoryTextField.text).matches()) ValidationInfo(
        "Invalid repository name. Name should consist of letters, numbers, dashes, dots and underscores",
        repositoryTextField)
      else null
    }

    return listOf({ accountInformationLoadingError },
                  chain({ notBlank(repositoryTextField, "No repository name selected") },
                        repositoryNamePatternMatchValidator,
                        existingRepoValidator),
                  chain({ notBlank(remoteTextField, "No remote name selected") },
                        existingRemoteValidator)
    ).mapNotNull { it() }
  }

  override fun getHelpId(): String = "github.share"
  override fun getDimensionServiceKey(): String = "Github.ShareDialog"
  override fun getPreferredFocusedComponent(): JBTextField = repositoryTextField

  fun getRepositoryName(): String = repositoryTextField.text
  fun getRemoteName(): String = remoteTextField.text
  fun isPrivate(): Boolean = privateCheckBox.isSelected
  fun getDescription(): String = descriptionTextArea.text
  fun getAccount(): GithubAccount = accountSelector.selectedItem as GithubAccount

  @TestOnly
  fun testSetRepositoryName(name: String) {
    repositoryTextField.text = name
  }
}
