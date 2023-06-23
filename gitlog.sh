#!/bin/bash

# Define the output file for the gitlog
gitlog_file="gitlog.md"

# Get content of the gitlog file to append text at the top
gitlog_content=$(cat "$gitlog_file")

> "gitlog_file"

current_branch=$(git rev-parse --abbrev-ref HEAD)

# Get the commit history using 'git log' command
git_log=$(git log "origin/$current_branch" "$current_branch" --pretty=format:"%h %aN %ae %ai")

# Iterate over each commit
while read commit_line; do

    # Get the commit message and author using 'git log' command
    commit=$(echo "$commit_line" | awk '{print $1}')
    commit_author=$(echo "$commit_line" | awk '{print $2}')
    commit_author_email=$(echo "$commit_line" | awk '{print $3}')
    commit_date=$(echo "$commit_line" | awk '{print $4" "$5}')
    commit_message=$(git log --format="%s" -n 1 "$commit")
    commit_refs=$(git log --format="%d" -n 1 "$commit")

    # Get the summary of changes for the commit using 'git show' command
    added_files=$(git show --stat --oneline --name-status "$commit" | grep "A" | awk '{print $2}')
    modified_files=$(git show --stat --oneline --name-status "$commit" | grep "M" | awk '{print $2}')
    deleted_files=$(git show --stat --oneline --name-status "$commit" | grep "D" | awk '{print $2}')

    # Append the commit information and summary to the gitlog file
    echo "## [$commit] - $commit_date " >> "$gitlog_file"
    echo "*Author*: $commit_author<$commit_author_email>  " >> "$gitlog_file"
    echo "*Refs*: $commit_refs  " >> "$gitlog_file"
    echo "> $commit_message  " >> "$gitlog_file"
    echo "" >> "$gitlog_file"

    # Process added files
    if [ -n "$added_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1 | awk '{print $1}')
            echo "- [+] [$file]($file) (+$lines)" >> "$gitlog_file"
        done <<< "$added_files"
    fi

    # Process modified files
    if [ -n "$modified_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1)
            added_lines=$(echo "$lines" | awk '{print $1}')
            deleted_lines=$(echo "$lines" | awk '{print $2}')
            echo "- [/] [$file]($file) (+$added_lines -$deleted_lines)" >> "$gitlog_file"
        done <<< "$modified_files"
    fi

    # Process deleted files
    if [ -n "$deleted_files" ]; then
        while IFS= read -r file; do
            echo "- [-] [$file]($file)" >> "$gitlog_file"
        done <<< "$deleted_files"
    fi

    echo "" >> "$gitlog_file"
done <<< "$git_log"

# Append the gitlog content at the end of the gitlog file
echo "$gitlog_content" >> "$gitlog_file"

echo "gitlog generated successfully: $gitlog_file"
