## Authorisation

By default, all workspaces are accessible by anybody who has access to your Structurizr installation.
Anonymous users (not signed in) have read-only access, while authenticated users (signed in) have read-write access.
The security of each workspace is summarised on the dashboard with a locked/unlocked icon.

### Configuring users and roles

Workspace access can be configured via the "Users" link on your workspace summary page.
Two text boxes provide a way to specify the usernames or role names that should have
read/write or read-only access.
Usernames and roles can also be specified via a regular expression.
For example, `^.*@example.com$` can be used to match everybody with an `@example.com` e-mail address.
Please note that regular expressions must be specified in the form, `^...$`.

If you'd prefer, you can also configure the set of users when creating your workspace using the Structurizr DSL,
via [users](https://github.com/structurizr/dsl/blob/master/docs/language-reference.md#users).
Some code-based libraries (e.g. [Structurizr for Java](https://github.com/structurizr/java/blob/master/structurizr-core/src/com/structurizr/configuration/WorkspaceConfiguration.java)) may also support this feature.
