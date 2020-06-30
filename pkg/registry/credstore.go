package registry

import (
	"encoding/base64"
	"encoding/json"
	"errors"
	"log"
	"strings"

	hspec "hyscale/pkg/hspec"

	"github.com/docker/cli/cli/config/configfile"
	"github.com/docker/cli/cli/config/types"
)

//GetAuthConfig retrieves the respective registy credentials from the docker config
func GetAuthConfig(hspecFiles []string) (*string, error) {

	RegistryMap := make(map[string]bool)

	for _, alias := range DockerHubAliases {
		RegistryMap[alias] = true
	}

	for _, each := range hspecFiles {
		hspec, err := hspec.LoadHspec(each)
		if err != nil {
			log.Fatal("Error while deserializing hspec", err)
		}

		if !RegistryMap[hspec.Image.Registry] {
			RegistryMap[hspec.Image.Registry] = true
		}

		if !RegistryMap[hspec.Image.BuildSpec.StackImage] {
			stackImgReg := getImageRegistry(hspec.Image.BuildSpec.StackImage)
			RegistryMap[stackImgReg] = true
		}

		if hspec.Agents != nil {
			for _, each := range *hspec.Agents {
				if !RegistryMap[each.Name] {
					imgReg := getImageRegistry(each.Name)
					RegistryMap[imgReg] = true
				}
			}
		}
	}

	// Constructing all auth configs for the unique registries
	auth := make([]types.AuthConfig, 5)
	for key := range RegistryMap {
		//TODO read verbose from external configuration
		regCreds := GetRegistryCredentials(key, false)
		if regCreds != (types.AuthConfig{}) {
			auth = append(auth, regCreds)
		}
	}

	data, err := convert(auth)
	if err != nil {
		log.Fatal("Error while reading config.json")
	}

	authString, err := json.Marshal(data)
	if err != nil {
		log.Fatal("Error while deserializing auth ")
	}
	json := string(authString)
	return &json, nil
}

//GetRegistryCredentials fetches the registry credentials from docker config json
func GetRegistryCredentials(registry string, verbose bool) types.AuthConfig {
	if registry == "" {
		log.Fatal("Invalid Registry")
	}

	auth, err := GetCredentials(registry, verbose)
	if err != nil {
		log.Fatal(errors.New("Error while fetching credentials for registry"), registry, err)
	}

	return auth
}

func convert(auths []types.AuthConfig) (*configfile.ConfigFile, error) {
	if auths == nil {
		// Message
	}

	authConfigs := make(map[string]types.AuthConfig)
	for _, each := range auths {
		tmp := each.Username + ":" + each.Password
		each.Auth = base64.StdEncoding.EncodeToString([]byte(tmp))
		each.Username = ""
		each.Password = ""
		authConfigs[each.ServerAddress] = each
	}
	configfile := configfile.ConfigFile{
		AuthConfigs: authConfigs,
	}
	return &configfile, nil
}

func getImageRegistry(img string) string {
	parts := strings.Split(strings.Split(img, ":")[0], "/")
	return parts[0]
}
