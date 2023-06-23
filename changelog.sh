#!/bin/bash

# Define the output file for the changelog
changelog_file="changelog.md"

# Start with a clean changelog file
> "$changelog_file"

# Legend for change types
echo "Legend:" >> "$changelog_file"
echo "- Added files: [+]" >> "$changelog_file"
echo "- Changed files: [/]" >> "$changelog_file"
echo "- Deleted files: [-]" >> "$changelog_file"
echo "" >> "$changelog_file"

# Get the commit history using 'git log' command
commits=$(git log --pretty=format:"%H" --reverse)

# Iterate over each commit
for commit in $commits; do
    # Get the commit message and author using 'git log' command
    commit_message=$(git log --format="%s" -n 1 "$commit")
    commit_author=$(git log --format="%an" -n 1 "$commit")

    # Get the summary of changes for the commit using 'git show' command
    added_files=$(git show --stat --oneline --name-status "$commit" | grep "A" | awk '{print $2}')
    modified_files=$(git show --stat --oneline --name-status "$commit" | grep "M" | awk '{print $2}')
    deleted_files=$(git show --stat --oneline --name-status "$commit" | grep "D" | awk '{print $2}')

    # Append the commit information and summary to the changelog file
    echo "Commit: $commit" >> "$changelog_file"
    echo "Author: $commit_author" >> "$changelog_file"
    echo "Message: $commit_message" >> "$changelog_file"
    echo "" >> "$changelog_file"

    # Process added files
    if [ -n "$added_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1 | awk '{print $1}')
            echo "- [+] [$file]($file) (+$lines)" >> "$changelog_file"
        done <<< "$added_files"
    fi

    # Process modified files
    if [ -n "$modified_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1 | awk '{print $1}')
            echo "- [/] [$file]($file) (/$lines)" >> "$changelog_file"
        done <<< "$modified_files"
    fi

    # Process deleted files
    if [ -n "$deleted_files" ]; then
        while IFS= read -r file; do
            echo "- [-] [$file]($file)" >> "$changelog_file"
        done <<< "$deleted_files"
    fi

    echo "" >> "$changelog_file"
done

echo "Changelog generated successfully: $changelog_file"
