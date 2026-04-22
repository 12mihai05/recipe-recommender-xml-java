<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" indent="yes"/>

    <xsl:param name="userSkill"/>

    <xsl:template match="/recipes">

        <html>
            <head>
                <title>Recipes (XSL View)</title>

                <style>
                    body {
                    margin: 0;
                    font-family: Arial, Helvetica, sans-serif;
                    background: linear-gradient(135deg, #f5f7fb, #e8eef8);
                    color: #1f2937;
                    }

                    .navbar {
                    position: sticky;
                    top: 0;
                    background: white;
                    padding: 14px 24px;
                    display: flex;
                    gap: 12px;
                    border-bottom: 1px solid #e5e7eb;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
                    }

                    .navbar a {
                    padding: 8px 14px;
                    border-radius: 8px;
                    background: #eff6ff;
                    color: #1d4ed8;
                    font-weight: 600;
                    text-decoration: none;
                    }

                    .navbar a:hover {
                    background: #dbeafe;
                    }

                    .page-container {
                    min-height: 100vh;
                    display: flex;
                    justify-content: center;
                    padding: 24px;
                    }

                    .card {
                    width: 95%;
                    max-width: 1000px;
                    background: #ffffff;
                    border-radius: 18px;
                    padding: 30px;
                    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
                    border: 1px solid #e5e7eb;
                    }

                    h1 {
                    margin-bottom: 20px;
                    }

                    table {
                    width: 100%;
                    border-collapse: collapse;
                    margin-top: 20px;
                    border-radius: 10px;
                    overflow: hidden;
                    }

                    th {
                    background: #3b82f6;
                    color: white;
                    text-align: left;
                    padding: 12px;
                    }

                    td {
                    padding: 12px;
                    border-bottom: 1px solid #e5e7eb;
                    }

                    tr:hover {
                    background: #f9fafb;
                    }

                    .match {
                    background-color: #fef9c3 !important;
                    }

                    .no-match {
                    background-color: #dcfce7 !important;
                    }

                    .back-btn {
                    display: inline-block;
                    margin-top: 20px;
                    padding: 10px 16px;
                    background: #3b82f6;
                    color: white;
                    text-decoration: none;
                    border-radius: 8px;
                    font-weight: 600;
                    }

                    .back-btn:hover {
                    background: #2563eb;
                    }
                </style>

            </head>

            <body>
                <div class="navbar">
                    <a href="/">Home</a>
                    <a href="/recipes">Recipes</a>
                    <a href="/recipes/add">Add Recipe</a>
                    <a href="/users">Users</a>
                    <a href="/users/add">Add User</a>
                    <a href="/recipes/by-cuisine">By Cuisine</a>
                    <a href="/recommendations">Recommendations</a>
                    <a href="/xsl/recipes">XSL View</a>
                </div>

                <div class="page-container">
                    <div class="card">

                        <h1>Recipes (XSL View)</h1>
                        <p class="user-skill-display">
                            Selected User Skill:
                            <xsl:value-of select="$userSkill"/>
                        </p>
                        <table>
                            <tr>
                                <th>Title</th>
                                <th>Cuisine 1</th>
                                <th>Cuisine 2</th>
                                <th>Difficulty</th>
                            </tr>

                            <xsl:for-each select="recipe">

                                <xsl:variable name="isMatch"
                                               select="difficulty = $userSkill"/>

                                <tr>
                                    <xsl:attribute name="class">
                                        <xsl:choose>
                                            <xsl:when test="$isMatch">match</xsl:when>
                                            <xsl:otherwise>no-match</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:attribute>

                                    <td><xsl:value-of select="title"/></td>
                                    <td><xsl:value-of select="cuisineType1"/></td>
                                    <td><xsl:value-of select="cuisineType2"/></td>
                                    <td><xsl:value-of select="difficulty"/></td>

                                </tr>

                            </xsl:for-each>

                        </table>

                        <a href="/xsl/recipes/view" class="back-btn">
                            ← Back to User Selection
                        </a>

                    </div>
                </div>

            </body>
        </html>

    </xsl:template>

</xsl:stylesheet>