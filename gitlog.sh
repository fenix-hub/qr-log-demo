#!/bin/bash

echo -e "Processing gitlog..."

# Define the output file for the gitlog
gitlog_path="../../../"
gitlog_file="gitlog.md"

current_branch=$(git rev-parse --abbrev-ref HEAD)

# Get the commit history using 'git log' command
git_log=$(git log "origin/$current_branch..$current_branch" --pretty=format:"%h %aN %ae %ai")

# Create a temporary file
temp_file=$(mktemp)

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
    echo "## [$commit] - $commit_date " >> "$temp_file"
    echo "*Author*: $commit_author<$commit_author_email>  " >> "$temp_file"
    echo "*Refs*: $commit_refs  " >> "$temp_file"
    echo "> $commit_message  " >> "$temp_file"
    echo "" >> "$temp_file"

    # Process added files
    if [ -n "$added_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1 | awk '{print $1}')
            echo "- [+] [$file]($file) (+$lines)" >> "$temp_file"
        done <<< "$added_files"
    fi

    # Process modified files
    if [ -n "$modified_files" ]; then
        while IFS= read -r file; do
            lines=$(git show --numstat --oneline "$commit" -- "$file" | tail -1)
            added_lines=$(echo "$lines" | awk '{print $1}')
            deleted_lines=$(echo "$lines" | awk '{print $2}')
            echo "- [/] [$file]($file) (+$added_lines -$deleted_lines)" >> "$temp_file"
        done <<< "$modified_files"
    fi

    # Process deleted files
    if [ -n "$deleted_files" ]; then
        while IFS= read -r file; do
            echo "- [-] [$file]($file)" >> "$temp_file"
        done <<< "$deleted_files"
    fi

    echo "" >> "$temp_file"
done <<< "$git_log"

# Append the existing contents of the gitlog file to the temporary file
cat "$gitlog_path$gitlog_file" >> "$temp_file"

# Overwrite the gitlog file with the contents of the temporary file
mv "$temp_file" "$gitlog_path$gitlog_file"

echo "Gitlog generated successfully!"

echo -e "Committing..."

# shellcheck disable=SC2164
cd $gitlog_path
git add $gitlog_file
git commit -m "(changed) updated gitlog.md"

exit 0